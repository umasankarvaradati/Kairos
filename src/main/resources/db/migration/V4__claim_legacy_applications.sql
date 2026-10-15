UPDATE job_applications
SET user_id = (
    SELECT id
    FROM users
    ORDER BY created_at ASC, id ASC
    LIMIT 1
)
WHERE user_id IS NULL
  AND EXISTS (SELECT 1 FROM users);
