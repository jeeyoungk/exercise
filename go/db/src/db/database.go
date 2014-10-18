package db

type tree struct {
	root *node
}

type node struct {
	key    string
	values []string
	left   *node
	right  *node
}

type indexProperty struct {
	unique bool
}

// Database allows you to store data and create indexes.
type Database struct {
	data            map[string]map[string]string
	indexes         map[string]*tree
	indexProperties map[string]indexProperty
}

// NewDatabase creates a new Database.
func NewDatabase() *Database {
	return &Database{
		data:            make(map[string]map[string]string),
		indexes:         make(map[string]*tree),
		indexProperties: make(map[string]indexProperty),
	}
}

func (d *Database) Insert(key string, value map[string]string) bool {
	// check for the unique constraints.
	for columnName, indexProperty := range d.indexProperties {
		if !indexProperty.unique {
			continue
		}
		index := d.indexes[columnName]
		if columnValue, ok := value[columnName]; ok {
			prev := index.find(columnValue)
			if len(prev) == 0 {
				// no result - safe to insert.
				continue
			} else if len(prev) == 1 && prev[0] == key {
				// has result that's not my key - not safe to insert.
				continue
			}
			// constraint failure
			return false
		}
	}
	if _, ok := d.data[key]; ok {
		d.Remove(key)
	}
	d.data[key] = value
	for indexName, index := range d.indexes {
		if columnValue, hasColumn := value[indexName]; hasColumn {
			index.insert(columnValue, key)
		}
	}
	return true
}

func (d *Database) Remove(key string) bool {
	prevVal, ok := d.data[key]
	if ok {
		delete(d.data, key)

		for indexName, index := range d.indexes {
			if columnValue, hasIndexedProperty := prevVal[indexName]; hasIndexedProperty {
				index.remove(columnValue, key)
			}
		}
		return true
	}
	return false
}

func (d *Database) Get(key string) map[string]string {
	value, ok := d.data[key]
	if ok {
		return value
	}
	return nil
}

func (d *Database) CreateIndex(column string, unique bool) bool {
	if _, ok := d.indexes[column]; ok {
		return true
	}
	t := &tree{}

	for pkey, value := range d.data {
		columnValue, hasKey := value[column]
		if !hasKey {
			continue
		}
		n := t.insert(columnValue, pkey)
		if unique && len(n.values) > 1 {
			return false
		}
	}

	d.indexes[column] = t
	d.indexProperties[column] = indexProperty{unique: unique}
	return true
}

func (d *Database) FetchIndex(column string, columnValue string) []string {
	if index, hasIndex := d.indexes[column]; hasIndex {
		return index.find(columnValue)
	}
	return nil
}

func (d *Database) FetchRange(column string, start string, end string) []string {
	if index, hasIndex := d.indexes[column]; hasIndex {
		if index.root == nil {
			return nil
		}
		var result = []string{}
		index.root.traverse(start, end, func(n node) {
			result = append(result, n.values...)
		})
		return result

	}
	return nil
}

func newNode(key string, value string) *node {
	return &node{
		key:    key,
		values: []string{value},
		left:   nil,
		right:  nil,
	}
}

func (t *tree) find(key string) []string {
	if t.root == nil {
		return []string{}
	}
	var node = t.root
	for {
		if node.key == key {
			return node.values
		} else if node.key > key {
			if node.left == nil {
				return []string{}
			}
			node = node.left
		} else {
			if node.right == nil {
				return []string{}
			}
			node = node.right
		}
	}
}

func (n *node) traverse(start string, end string, iterator func(node)) {
	if n.left != nil && n.key >= start {
		n.left.traverse(start, end, iterator)
	}
	if start <= n.key && n.key <= end {
		iterator(*n)
	}
	if n.right != nil && n.key <= end {
		n.right.traverse(start, end, iterator)
	}
}

func (t *tree) remove(key string, value string) bool {
	if t.root == nil {
		return false
	}
	var removed bool
	var n = t.root
	for {
		if n.key == key {
			n.values, removed = removeSlice(n.values, value)
			return removed
		} else if n.key < key {
			if n.left == nil {
				return false
			}
			n = n.left
		} else {
			if n.right == nil {
				return false
			}
			n = n.right
		}
	}
}

// insert value to the tree, and return the resulting node.
func (t *tree) insert(key string, value string) *node {
	if t.root == nil {
		t.root = newNode(key, value)
		return t.root
	}
	var node = t.root
	for {
		if node.key == key {
			// insert in place
			var found = false
			for _, v := range node.values {
				if v == value {
					found = true
					break
				}
			}
			if !found {
				node.values = append(node.values, value)
			}
			return node
		} else if node.key > key {
			if node.left == nil {
				node.left = newNode(key, value)
				return node.left
			}
			node = node.left
		} else {
			if node.right == nil {
				node.right = newNode(key, value)
				return node.right
			}
			node = node.right
		}
	}
}

func removeSlice(original []string, value string) ([]string, bool) {
	for index, v := range original {
		if v == value {
			return append(original[:index], original[index+1:]...), true
		}
	}
	return original, false
}
