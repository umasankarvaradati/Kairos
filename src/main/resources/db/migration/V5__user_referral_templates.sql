CREATE TABLE user_referral_templates (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    template TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

INSERT INTO user_referral_templates (user_id, template, created_at, updated_at)
SELECT id,
       'Hi {{name}},' || chr(10) || chr(10) ||
       'I' || chr(8217) || 'm applying for the {{jobTitle}} role at {{company}}. Would you be open to referring me?' || chr(10) || chr(10) ||
       'Thanks,' || chr(10) || '{{senderName}}',
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM users;

UPDATE referrals referral
SET message_template = user_template.template
FROM user_referral_templates user_template
WHERE user_template.user_id = referral.user_id
  AND (referral.message_template IS NULL OR btrim(referral.message_template) = '');
