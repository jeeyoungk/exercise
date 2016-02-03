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

public:
  void put(const K &key, const V &value) {
    if (!root) {
      root = unique_ptr<Node<K, V>>(new Node<K, V>(key, value));
      return;
    }
    auto ptr = root.get(); // traversing unique_ptr.
    while (ptr) {
      if (ptr->key > key) {
        if (ptr->left) {
          ptr = ptr->left.get();
        } else {
          ptr->left = unique_ptr<Node<K, V>>(new Node<K, V>(key, value));
          break;
        }
      } else if (ptr->key < key) {
        if (ptr->right) {
          ptr = ptr->right.get();
        } else {
          ptr->right = unique_ptr<Node<K, V>>(new Node<K, V>(key, value));
          break;
        }
      } else {
        ptr->value = value;
        break;
      }
    }
  }
  bool remove(const K &key) {
    if (!root) {
      return false;
    }
    unique_ptr<Node<K, V>> *parentptr = &root;
    Node<K, V> *ptr = root.get();
    while (ptr) {
      if (ptr->key > key) {
        if (ptr->left) {
          parentptr = &(ptr->left);
          ptr = ptr->left.get();
        } else {
          return false;
        }
      } else if (ptr->key < key) {
        if (ptr->right) {
          parentptr = &(ptr->right);
          ptr = ptr->right.get();
        } else {
          return false;
        }
      } else {
        // found the element!
        if (!ptr->left && ptr->right) {
          *parentptr = move(ptr->right);
        } else if (!ptr->right && ptr->left) {
          *parentptr = move(ptr->left);
        } else if (!ptr->left && !ptr->right) {
          parentptr->reset();
        } else {
          // contains both :(. remove and append to right.
          auto right = move(ptr->right);
          *parentptr = move(ptr->left);
          Node<K, V>* appendpoint = parentptr->get();
          while (appendpoint->right) {
            appendpoint = appendpoint->right.get();
          }
          (appendpoint)->right = move(right);
        }
        return true;
      }
    }
    return false;
  }
  bool contains(const K &key) const {
    if (!root) {
      return false;
    }
    auto ptr = root.get(); // traversing unique_ptr.
    while (ptr) {
      if (ptr->key > key) {
        if (ptr->left) {
          ptr = ptr->left.get();
        } else {
          return false;
        }
      } else if (ptr->key < key) {
        if (ptr->right) {
          ptr = ptr->right.get();
        } else {
          return false;
        }
      } else {
        return true;
      }
    }
    return false;
  }
  const V &get(const K &key) {
    if (!root) {
      return EMPTY;
    }
    auto ptr = root.get(); // traversing unique_ptr.
    while (ptr) {
      if (ptr->key > key) {
        if (ptr->left) {
          ptr = ptr->left.get();
        } else {
          return EMPTY;
        }
      } else if (ptr->key < key) {
        if (ptr->right) {
          ptr = ptr->right.get();
        } else {
          return EMPTY;
        }
      } else {
        return ptr->value;
      }
    }
    return EMPTY;
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

int main() {
  Tree<int, string> t;
  string foo = "foo";
  for (auto i = 0; i < 10; i++) {
    for (auto j = 0; j < 10; j++) {
      t.put(i + j * 10, foo);
    }
  }
  cout << "size:  " << t.size() << endl;
  cout << "depth: " << t.depth() << endl;
  for (auto i = 0; i < 100; i++) {
    if (!t.contains(i)) {
      cout << "does not contain " << i << endl;
    }
    if (t.get(i) != foo) {
      cout << "does not contain the correct value " << i << endl;
    }
  }
  for (auto j = 9; j >= 0; j--) {
    for (auto i = 9; i >= 0; i--) {
      t.remove(i + j * 10);
    }
  }
  Tree<int, string> t2;
  t2.put(1, "a");
  t2.put(0, "b");
  t2.put(2, "c");
  t2.remove(1);
  t2.remove(0);
  t2.remove(2);
  cout << t2.size() << endl;
  cout << t2.depth() << endl;
}
