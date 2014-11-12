package db

import (
	"fmt"
	"reflect"
	"testing"
)

func fruit(fruit string) map[string]string {
	value := make(map[string]string)
	value["fruit"] = fruit
	return value
}
func TestUniqueIndex(t *testing.T) {
	d := NewDatabase()
	d.Insert("x", fruit("apple"))
	d.Insert("y", fruit("banana"))
	if !d.CreateIndex("fruit", true) {
		t.Error("Index creation failed.")
	}

	result := d.FetchIndex("fruit", "apple")
	if !reflect.DeepEqual(result, []string{"x"}) {
		t.Error("Fetching wrong items.")
	}

	if d.Insert("z", fruit("banana")) {
		t.Error("Expected failure")
	}
	if d.Get("z") != nil {
		t.Error("Expected nil")
	}
	if !reflect.DeepEqual(d.FetchIndex("fruit", "banana"), []string{"y"}) {
		t.Error("Expected different result.")
	}
	if !d.Insert("z", fruit("cherry")) {
		t.Error("Expected success")
	}
}

func TestRange(t *testing.T) {
	d := NewDatabase()
	insert := func(i int) {
		key := fmt.Sprintf("key-%02d", i)
		value := make(map[string]string)
		value["column"] = fmt.Sprintf("column-%02d", i)
		d.Insert(key, value)
	}
	for i := 1; i < 20; i++ {
		insert(i)
	}
	d.CreateIndex("column", false)
	if !reflect.DeepEqual(d.FetchRange("column", "column-04", "column-08"),
		[]string{"key-04", "key-05", "key-06", "key-07", "key-08"}) {
		t.Error("Expected different result.")
	}
}
