package main

import (
	"db"
	"fmt"
)

func main() {
	d := db.NewDatabase()
	fooVar := make(map[string]string)
	barVar := make(map[string]string)
	bazVar := make(map[string]string)
	fooVar["fruit"] = "apple"
	barVar["fruit"] = "banana"
	bazVar["fruit"] = "banana"
	d.Insert("foo", fooVar)
	d.Insert("bar", barVar)
	d.Insert("baz", bazVar)
	fmt.Printf("Creating unique index: %t (expecting false)\n", d.CreateIndex("fruit", true))
	fmt.Printf("Creating non-unique index: %t (expecting true)\n", d.CreateIndex("fruit", false))
	fmt.Printf("Expecting foo: %s\n", d.FetchIndex("fruit", "apple"))
	d.Remove("bar")
	d.Remove("foo")
	fmt.Println(d.FetchIndex("fruit", "apple"))
	fmt.Println(d.FetchIndex("fruit", "banana"))
	insert := func(i int) {
		key := fmt.Sprintf("key-%02d", i)
		value := make(map[string]string)
		value["column"] = fmt.Sprintf("column-%02d", i)
		d.Insert(key, value)
	}
	insert(1)
	insert(5)
	insert(4)
	insert(8)
	insert(12)
	insert(17)
	insert(24)
	d.CreateIndex("column", false)
	fmt.Println("Fetching single index:")
	fmt.Println(d.FetchIndex("column", "column-01"))
	fmt.Println("Fetching range of value:")
	fmt.Println(d.FetchRange("column", "column-04", "column-09"))
}
