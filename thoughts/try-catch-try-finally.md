# `try-catch` vs `try-finally`

`try-catch` and `try-finally` looks similar, but they model two different workflow.

* `try-catch` is used to act on specific types of errors.
* `try-finally` is used to ensure that a resource is cleaned afterwards. It is used to simulate [RAII](https://en.wikipedia.org/wiki/Resource_Acquisition_Is_Initialization).
* `try-catch` should be limited in scope to prevent accidental error catching.
* `try-finally`'s scope can be as large as the scope required for the resource.

## `try-catch` example
```java
// this is code smell
try {
  operationA() // throws SpecificException
  operationB() // throws SpecificException
} catch (SpecificException e) {
  // which one failed? A or B?
}
```


## `try-finally` example
```java
Resource r = allocate();
try {
  // use r.
} finally {
  r.free();
}
```

## Open questions

* Should we ever try to use `try-catch-finally`?
* How about `try-catch-else`?

```python
try:
  operationA()
catch SpecificException, e:
  # error case
else:
  # success case
```

```java
boolean success = false;
try {
  operationA();
  success = true;
} catch (SpeficException e) {
  // error case
}
if (success) {
  // success case
}
```
