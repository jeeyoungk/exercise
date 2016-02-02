#include <cstdint>
#include <iostream>
#include <unordered_map>
#include <unordered_set>

using namespace std;

template <typename KeyT, typename ColumnT, typename ValueT> class Database {
  unordered_map<KeyT, unordered_map<ColumnT, ValueT>> db;
  unordered_map<ColumnT, unordered_map<ValueT, unordered_set<KeyT>>> index;
  const unordered_set<KeyT> EMPTY_RESULT;
  const unordered_map<ColumnT, ValueT> EMPTY_ROW;

public:
  void put(const KeyT &key, const unordered_map<ColumnT, ValueT> &row) {
    remove(key);
    db[key] = row;
    for (auto indexItr = index.begin(); indexItr != index.end(); ++indexItr) {
      auto column = indexItr->first;
      auto columnItr = row.find(column);
      if (columnItr != row.end()) {
        auto value = columnItr->second;
        indexItr->second[value].insert(key);
      }
    }
  }
  bool remove(const KeyT &key) {
    auto iter = db.find(key);
    if (iter == db.end()) {
      return false;
    } else {
      auto row = iter->second;
      for (auto indexItr = index.begin(); indexItr != index.end(); ++indexItr) {
        auto columnItr = row.find(indexItr->first);
        if (columnItr != row.end()) {
          auto value = columnItr->second;
          indexItr->second[value].erase(key);
        }
      }
      db.erase(iter);
      return true;
    }
  }
  const unordered_map<ColumnT, ValueT> &get(const KeyT &key) const {
    auto iter = db.find(key);
    if (iter != db.end()) {
      return iter->second;
    } else {
      return EMPTY_ROW;
    }
  }
  void createIndex(const ColumnT &column) {
    if (index.find(column) != index.end()) {
      return;
    }
    unordered_map<ValueT, unordered_set<KeyT>> &curIndex = index[column];
    for (auto entry = db.begin(); entry != db.end(); ++entry) {
      auto valueItr = entry->second.find(column);
      if (valueItr != entry->second.end()) {
        curIndex[valueItr->second].insert(entry->first);
      }
    }
  }
  const unordered_set<KeyT> &fetch(const ColumnT &column,
                                   const ValueT &value) const {
    auto indexItr = index.find(column);
    if (indexItr == index.end()) {
      return EMPTY_RESULT;
    }
    auto rangeItr = indexItr->second.find(value);
    if (rangeItr == indexItr->second.end()) {
      return EMPTY_RESULT;
    }
    return rangeItr->second;
  }
};

template <typename T> void print(unordered_set<T> set) {
  cout << "{";
  for (auto itr = set.begin(); itr != set.end(); ++itr) {
    if (itr != set.begin()) {
      cout << ",";
    }
    cout << *itr;
  }
  cout << "}" << endl;
}

int main() {
  Database<int32_t, string, string> db;
  unordered_map<string, string> row1, row2, row3, row4;
  row1["name"] = "amy";
  row2["name"] = "john";
  row3["name"] = "park";
  row4["name"] = "park";
  db.put(1, row1);
  db.put(2, row2);
  db.put(3, row3);
  db.createIndex("name");
  db.put(4, row4);
  db.remove(3);
  row1["name"] = "sarah";
  db.put(1, row1);
  db.get(1);
  print(db.fetch("name", "amy"));
  print(db.fetch("name", "sarah"));
  print(db.fetch("name", "john"));
  print(db.fetch("name", "park"));
  return 0;
}
