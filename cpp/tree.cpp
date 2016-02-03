#include <cstdint>
#include <iostream>
#include <memory>

using namespace std;

template <typename K, typename V> class Node {
public:
  Node(const K &key, const V &value) : key(key), value(value) {
    this->left = unique_ptr<Node<K, V>>(nullptr);
    this->right = unique_ptr<Node<K, V>>(nullptr);
  }
  const K key;
  V value;
  unique_ptr<Node<K, V>> left;
  unique_ptr<Node<K, V>> right;

  // recursive method.
  uint64_t size() const {
    return 1 + (left ? left->size() : 0) + (right ? right->size() : 0);
  }
  // recursive method.
  uint64_t depth() const {
    return 1 + max(left ? left->depth() : 0, right ? right->depth() : 0);
  }
};

template <typename K, typename V> class Tree {
  unique_ptr<Node<K, V>> root;
  const V EMPTY;
  // find a reference to the pointer that is either:
  // - contains a node with the given key.
  // - unassigned pointer that can contain the given key.
  const unique_ptr<Node<K, V>> &find(const K &key) const {
    const unique_ptr<Node<K, V>> *ptr = &root;
    while (*ptr) {
      const Node<K, V> &node = *(*ptr);
      if (node.key > key) {
        if (node.left) {
          ptr = &(node.left);
        } else {
          return node.left;
        }
      } else if (node.key < key) {
        if (node.right) {
          ptr = &(node.right);
        } else {
          return node.right;
        }
      } else {
        return *ptr;
      }
    }
    return root;
  }

  unique_ptr<Node<K, V>> &find(const K &key) {
    return const_cast<unique_ptr<Node<K, V>> &>(
        static_cast<const Tree<K, V> &>(*this).find(key));
  }

public:
  void put(const K &key, const V &value) {
    unique_ptr<Node<K, V>> &ptr = find(key);
    if (ptr) {
      ptr->value = value;
    } else {
      ptr.reset(new Node<K, V>(key, value));
    }
  }

  bool remove(const K &key) {
    unique_ptr<Node<K, V>> &ptr = find(key);
    if (!ptr) {
      return false;
    } else if (!ptr->left && ptr->right) {
      ptr = move(ptr->right);
    } else if (!ptr->right && ptr->left) {
      ptr = move(ptr->left);
    } else if (!ptr->left && !ptr->right) {
      ptr.reset();
    } else {
      // contains both :(. remove and append to right.
      auto right = move(ptr->right);
      ptr = move(ptr->left);
      Node<K, V> *appendptr = ptr.get();
      while (appendptr->right) {
        appendptr = appendptr->right.get();
      }
      appendptr->right = move(right);
    }
    return true;
  }

  bool contains(const K &key) const {
    return find(key) ? true : false;
  }

  const V &get(const K &key) {
    const unique_ptr<Node<K, V>> &ptr = find(key);
    return ptr ? ptr-> value : EMPTY;
  }

  const uint64_t size() const {
    if (!root) {
      return 0;
    }
    return root->size();
  }

  const uint64_t depth() const {
    if (!root) {
      return 0;
    }
    return root->depth();
  }
};

bool test1() {
  Tree<int, string> t;
  t.put(1, "b");
  t.put(0, "a");
  t.put(2, "c");
  if (t.size() != 3) {
    return false;
  }
  for (auto i = 0; i < 3; i++) {
    if (!t.contains(i)) {
      return false;
    }
  }
  if (t.get(0) != "a") {
    return false;
  }
  if (t.get(1) != "b") {
    return false;
  }
  if (t.get(2) != "c") {
    return false;
  }
  if (t.contains(-1)) {
    return false;
  }
  if (t.contains(4)) {
    return false;
  }
  t.remove(1);
  t.remove(0);
  t.remove(2);
  if (t.size() != 0) {
    return false;
  }
  return true;
}

bool test2() {
  auto count = 0;
  Tree<int, string> t;
  for (auto i = 0; i < 10; i++) {
    for (auto j = 0; j < 10; j++) {
      t.put(i + j * 10, "value");
      count++;
    }
    if (t.size() != count) {
      return false;
    }
  }
  if (t.size() != 100) {
    return false;
  }
  return true;
}

bool test3() {
  auto count = 0;
  Tree<int, string> t;
  for (auto i = 0; i < 10; i++) {
    for (auto j = 0; j < 10; j++) {
      t.put(j, "value");
    }
    if (t.size() != 10) {
      return false;
    }
  }
  return true;
}

int main() {
  cout << test1() << endl;
  cout << test2() << endl;
  cout << test3() << endl;
}
