-- ============================================================
--  CLASS MARKET — Banco de dados MySQL
--  Como rodar: mysql -u root -p < sql/classmarket.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS classmarket
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE classmarket;

CREATE TABLE IF NOT EXISTS usuarios (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  nome          VARCHAR(120) NOT NULL,
  email         VARCHAR(150) NOT NULL UNIQUE,
  telefone      VARCHAR(20),
  curso         VARCHAR(80),
  senha         VARCHAR(255) NOT NULL,
  reset_token   VARCHAR(64)  DEFAULT NULL,
  reset_expira  DATETIME     DEFAULT NULL,
  ativo         TINYINT(1)   NOT NULL DEFAULT 1,
  criado_em     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categorias (
  id   INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(60) NOT NULL UNIQUE
);

INSERT IGNORE INTO categorias (nome) VALUES ('doces'), ('salgados'), ('bebidas');

CREATE TABLE IF NOT EXISTS produtos (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  nome         VARCHAR(120)  NOT NULL,
  descricao    TEXT,
  preco        DECIMAL(10,2) NOT NULL,
  estoque      INT           NOT NULL DEFAULT 0,
  categoria_id INT           NOT NULL,
  bloco        VARCHAR(10)   NOT NULL,
  sala         VARCHAR(10)   NOT NULL,
  foto_url     VARCHAR(500),
  vendedor_id  INT           NOT NULL,
  ativo        TINYINT(1)    NOT NULL DEFAULT 1,
  criado_em    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status       VARCHAR(20)   NOT NULL DEFAULT 'pendente',
  FOREIGN KEY (categoria_id) REFERENCES categorias(id),
  FOREIGN KEY (vendedor_id)  REFERENCES usuarios(id)
);

CREATE TABLE IF NOT EXISTS avaliacoes (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  tipo        ENUM('site','produto') NOT NULL,
  nota        TINYINT NOT NULL,
  comentario  TEXT,
  usuario_id  INT DEFAULT NULL,
  produto_id  INT DEFAULT NULL,
  criado_em   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status       VARCHAR(20)   NOT NULL DEFAULT 'pendente',
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
  FOREIGN KEY (produto_id) REFERENCES produtos(id) ON DELETE CASCADE
);

-- ADM padrao (senha sera configurada pelo Java na primeira execucao)
INSERT IGNORE INTO usuarios (nome, email, senha)
VALUES ('Administrador', 'classmarket@proton.me', 'TROCAR_VIA_JAVA');



-- ── Seed de produtos de exemplo ─────────────────────────────────────────
-- IMPORTANTE: insere depois de o ADM existir (INSERT IGNORE acima já o criou)
-- A senha do ADM será configurada pelo Java na primeira execução.

-- Garante que o ADM existe antes dos produtos (que referenciam o vendedor_id)
SET @adm_id = (SELECT id FROM usuarios WHERE email = 'classmarket@proton.me' LIMIT 1);

INSERT IGNORE INTO produtos (nome, descricao, preco, estoque, categoria_id, bloco, sala, foto_url, vendedor_id) VALUES
  ('Brownie de Chocolate',    'Brownie caseiro crocante por fora e cremoso por dentro.',          4.50, 20, 1, 'A', '101', 'https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=500', @adm_id),
  ('Bolo de Cenoura',         'Fatia generosa com cobertura de brigadeiro.',                      5.00, 15, 1, 'B', '205', 'https://images.unsplash.com/photo-1621303837174-89787a7d4729?w=500', @adm_id),
  ('Coxinha de Frango',       'Coxinha crocante recheada com frango desfiado temperado.',         3.50, 30, 2, 'C', '12',  'https://images.unsplash.com/photo-1599599810769-bcde5a160d32?w=500', @adm_id),
  ('Esfiha de Carne',         'Esfiha aberta com carne moída temperada.',                         3.00, 25, 2, 'A', '102', 'https://images.unsplash.com/photo-1639024471283-03518883512d?w=500', @adm_id),
  ('Suco de Maracujá',        'Suco natural gelado de maracujá com 300 ml.',                     6.00, 10, 3, 'B', '200', 'https://images.unsplash.com/photo-1546173159-315724a31696?w=500', @adm_id),
  ('Água Mineral 500ml',      'Água mineral sem gás gelada.',                                     2.00, 50, 3, 'C', '10',  'https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=500', @adm_id),
  ('Brigadeiro Gourmet',      'Brigadeiro artesanal com granulado belga.',                        2.50, 40, 1, 'C', '15',  'https://images.unsplash.com/photo-1551024601-bec78aea704b?w=500', @adm_id),
  ('Pastel de Queijo',        'Pastel frito crocante com queijo derretido.',                      4.00, 20, 2, 'A', '103', 'https://images.unsplash.com/photo-1604382354936-07c5d9983bd3?w=500', @adm_id),
  ('Vitamina de Banana',      'Vitamina cremosa de banana com leite e mel.',                      5.50,  8, 3, 'B', '201', 'https://images.unsplash.com/photo-1553530666-ba11a7da3888?w=500', @adm_id),
  ('Pão de Queijo (unid.)',   'Pão de queijo mineiro quentinho, feito na hora.',                  2.00, 60, 2, 'C', '14',  'https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=500', @adm_id);

-- Aprovação automática dos produtos do seed
UPDATE produtos SET status = 'aprovado';
