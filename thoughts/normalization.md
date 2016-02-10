# Normalization and index hit

Consider the following database of tasks.

* Tasks has `task_type` (a small finite set of unique symbols) and `payload` associated with individual tasks.
* Tasks can be attempted many times - the application will attempt until it succeeds.
* Tasks and Attempts form one-to-many relationship via foreign key.

```
tasks (id, task_type, payload)
attempts (id, task_id (foreign key to tasks.id), status, attempted_at)
```

I want to perform the following question efficiently:

Givein a `task_type` find the latest attempt.

Which I write the following query:
```
select attempts.id from attempts
  inner join tasks on tasks.id = attempts.task_id
  where attempts.task_type = ?
  order by attempts.attempted_at desc
  limit 1;
```

I argue that there's no way to make this query efficient unless we denormalize `task_type` into `attempts`.
Then a composite lexicographical index (attempts.task_type, attempts.attempted_at) can be created to make the query faster.
