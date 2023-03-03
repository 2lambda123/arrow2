---
layout: docs-optics
title: Lens
permalink: /optics/lens/
---

## Lens


Optics are essentially abstractions to update immutable data structures in an elegant way.
A `Lens` (aka functional reference) is an optic that can focus into a structure and `get`, `modify`, or `set` its focus (target). They're mostly used for `product types` such as a `data class` or a `TupleN`.

Lenses can be seen as a pair of functions, a getter, and a setter. A `Lens<S, A>` represents a getter: `get: (S) -> A`, and `setter: (A) -> (S) -> S`, where `S` is called the source of the `Lens` and `A` is called the focus or target of the `Lens`.

Given a simple structure `Player`, we can create a `Lens<Player, Int>` to get, set, or modify its value.

```kotlin
import arrow.optics.*

data class Player(val health: Int)

val playerLens: Lens<Player, Int> = Lens(
    get = { player -> player.health },
    set = { player, value -> player.copy(health = value) }
)

val player = Player(70)
```
```kotlin
playerLens.get(player)
```
```kotlin
playerLens.set(player, 100)
```
```kotlin
playerLens.modify(player) { it - 20 }
```

We can also `lift` above function `(Int) -> Int` to `(Player) -> Player`.

```kotlin
val lift: (Player) -> Player = playerLens.lift { it + 10 }
lift(player)
```

### Composition

By composing lenses, we can create a telescope that allows us to focus in on nested structures.

At first sight, a `Lens` does not seem very useful, as it is just a getter/setter with some convenience methods. But lenses solve a couple of problems such as the composition of getters and setters. By default, getters and setters do not compose, and dealing with nested structures can be cumbersome.

Let's examine the following example. We have an `Employee`, and he works for a certain `Company` located at a certain `Address` on a `Street`. And, as a business requirement, we have to capitalize `Street::name` in order to print nicer business cards.

```kotlin
data class Street(val number: Int, val name: String)
data class Address(val city: String, val street: Street)
data class Company(val name: String, val address: Address)
data class Employee(val name: String, val company: Company)
```
```kotlin
val employee = Employee("John Doe", Company("Arrow", Address("Functional city", Street(23, "lambda street"))))
employee
```

Without lenses, we could use the `copy` method provided on a `data class` for dealing with immutable structures.

```kotlin
employee.copy(
        company = employee.company.copy(
                address = employee.company.address.copy(
                        street = employee.company.address.street.copy(
                                name = employee.company.address.street.name.capitalize()
                        )
                )
        )
)
```

As we can immediately see, this is hard to read, does not scale very well, and it draws attention away from the simple operation we wanted to do `name.capitalize()`.

What we actually wanted to do here is the following: focus into employee's company, `and then` focus into the company's address, `and then` focus into the street address, and finally, modify the street name by capitalizing it.

```kotlin
val employeeCompany: Lens<Employee, Company> = Lens(
        get = { it.company },
        set = { employee, company -> employee.copy(company = company) }
)

val companyAddress: Lens<Company, Address> = Lens(
        get = { it.address },
        set = { company, address -> company.copy(address = address) }
)

val addressStreet: Lens<Address, Street> = Lens(
        get = { it.street },
        set = { address, street -> address.copy(street = street) }
)

val streetName: Lens<Street, String> = Lens(
        get = { it.name },
        set = { street, name -> street.copy(name = name) }
)

val employeeStreetName: Lens<Employee, String> = employeeCompany compose companyAddress compose addressStreet compose streetName

employeeStreetName.modify(employee, String::capitalize)
```

Don't worry about the boilerplate of the lenses written above because it can be generated by Arrow. So we've essentially replaced our original snippet with the last two lines.

`Lens` can be composed with all optics and result in the following optics:

|   | Iso | Lens | Prism |Optional | Getter | Setter | Fold | Traversal |
| --- | --- | --- | --- |--- | --- | --- | --- | --- |
| Lens | Lens | Lens | Optional | Optional | Getter | Setter | Fold | Traversal |

### Generating lenses

Lenses can be generated for a `data class` by the `@optics` annotation. For every constructor parameter of the `data class`, a `Lens` will be generated.
The lenses will be generated as extension properties on the companion object `val T.Companion.paramName`.

```kotlin
@optics data class Account(val balance: Int, val available: Int) {
  companion object
}
```

For `Account`, two lenses will be generated: `val Account.Companion.balance: Lens<Account, Int>` and `val Account.Companion.available: Lens<Account, Int>`.

```kotlin
val balanceLens: Lens<Account, Int> = Account.balance
```

### Polymorphic lenses <a id="Plens"></a>
When dealing with polymorphic product types, we can also have polymorphic lenses that allow us to morph the type of the focus (and, as a result, the constructed type) of our `PLens`. The following method is also available as `PLens.pPairFirst<A, B, R>()` in the `arrow.optics` package.

```kotlin
fun <A, B, R> pair(): PLens<Pair<A, B>, Pair<R, B>, A, R> = PLens(
        { it.first },
        { ab, r -> r to ab.second }
)

PLens.pairPFirst<Int, String, String>().set(5 to "World", "Hello, ")
```

### Laws

Arrow provides [`LensLaws`][lenses_laws_source]{:target="_blank"} in the form of test cases for internal verification of lawful instances and third party apps creating their own lenses.

[lenses_laws_source]: https://github.com/arrow-kt/arrow/blob/main/arrow-libs/optics/arrow-optics-test/src/main/kotlin/arrow/optics/test/laws/LensLaws.kt