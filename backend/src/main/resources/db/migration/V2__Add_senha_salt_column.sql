-- Adiciona coluna para salt da senha
ALTER TABLE usuarios ADD COLUMN senha_salt VARCHAR(255);

-- Define valor padrão temporário para registros existentes
UPDATE usuarios SET senha_salt = 'legacy_salt' WHERE senha_salt IS NULL;

-- Torna a coluna obrigatória
ALTER TABLE usuarios ALTER COLUMN senha_salt SET NOT NULL;