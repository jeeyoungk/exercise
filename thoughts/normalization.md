# Normalization and index hit

Consider the following database of tasks.

* Tasks have `task_type` (a small finite set of unique symbols) and `payload` associated with individual tasks.
* Tasks can be attempted many times - the application will attempt until it succeeds.
* Tasks and Attempts form one-to-many relationship via a foreign key from attempts.

```
tasks (id, task_type, payload)
attempts (id, task_id (foreign key to tasks.id), status, attempted_at)
```

I want to perform the following question efficiently: **Given a `task_type` find the latest attempt for it**.

Which I write the following query:
```
select attempts.id from attempts
  inner join tasks on tasks.id = attempts.task_id
  where attempts.task_type = ?
  order by attempts.attempted_at desc
  limit 1;
```

I argue that there's no way to make this query efficient unless we denormalize `task_type` into `attempts`.

Hypothetically I can have two indices in these tables.

* index on `(tasks.task_type, tasks.id)`
* index on `(attempts.task_id)`

A sample query plan would look like this:

1. Query `tasks.id` for all `tasks.task_type`.
2. Join with `attempts.task_id`
3. Sort by `attempts.attempted_at`
4. Limit 1.

Step `(3)` will be relatively expensive as the sorted list is not stored in any index. All these rows needs to be read and sorted before the `limit` clause can be executed.

That is where denormalization can help, because a very specific index can serve its purpose. After adding `attempts.task_type`, a composite lexicographical index `(attempts.task_type, attempts.attempted_at)` can be created to make the query faster.
