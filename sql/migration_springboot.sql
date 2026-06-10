-- ============================================================
--  CLASS MARKET — Migration para Spring Boot
--  Rode este script APÓS classmarket.sql se já tiver o banco
--  OU substitua classmarket.sql por este arquivo completo
-- ============================================================

-- 1. Adicionar coluna status em produtos (se não existir)
ALTER TABLE produtos
  ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'pendente';

-- Aprovamos os produtos do seed (vendidos pelo ADM)
UPDATE produtos SET status = 'aprovado' WHERE status = 'pendente';

-- 2. Atualizar senha do ADM para BCrypt de "adm123"
--    Hash gerado por: BCryptPasswordEncoder().encode("adm123")
UPDATE usuarios
SET senha = '$2a$10$7EqJtq98hPqEX7fNZaFWoOe5UoRiZ8K3eXRGwXQXHlZh8y1UoWOOi'
WHERE email = 'classmarket@proton.me';

-- 3. Índices úteis
CREATE INDEX IF NOT EXISTS idx_produtos_status  ON produtos(status);
CREATE INDEX IF NOT EXISTS idx_produtos_ativo   ON produtos(ativo);
CREATE INDEX IF NOT EXISTS idx_avaliacoes_tipo  ON avaliacoes(tipo);
