# 覆盖equals时总要覆盖hashCode

> 根据`Object.hashCode`的通用约定，在每个覆盖了equals方法的类中，如果没有覆盖hashCode方法，将会导致该类无法结合所有基于散列的集合一起正常工作，这样的集合包括 HashMap、HashSet 和 HashTable。

