-- Extensão para UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabela de usuários
CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    nome VARCHAR(255) NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    ativo BOOLEAN NOT NULL DEFAULT true,
    dados_iniciais_carregados BOOLEAN NOT NULL DEFAULT false
);

-- Índices para usuários
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_ativo ON usuarios(ativo);

-- Tabela de transações
CREATE TABLE transacoes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    valor DECIMAL(15,2) NOT NULL CHECK (valor > 0),
    moeda VARCHAR(3) NOT NULL DEFAULT 'BRL',
    descricao TEXT NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('RECEITA', 'DESPESA')),
    data DATE NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    ativa BOOLEAN NOT NULL DEFAULT true
);

-- Índices para transações
CREATE INDEX idx_transacoes_usuario_data ON transacoes(usuario_id, data);
CREATE INDEX idx_transacoes_categoria ON transacoes(categoria);
CREATE INDEX idx_transacoes_tipo ON transacoes(tipo);
CREATE INDEX idx_transacoes_ativa ON transacoes(ativa);

-- Tabela de orçamentos
CREATE TABLE orcamentos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    categoria VARCHAR(50) NOT NULL,
    limite DECIMAL(15,2) NOT NULL CHECK (limite > 0),
    periodo VARCHAR(20) NOT NULL CHECK (periodo IN ('MENSAL', 'TRIMESTRAL', 'ANUAL')),
    gasto_atual DECIMAL(15,2) NOT NULL DEFAULT 0 CHECK (gasto_atual >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO', 'INATIVO', 'EXCEDIDO')),
    inicio_periodo DATE NOT NULL,
    fim_periodo DATE NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT uk_orcamento_usuario_categoria_periodo UNIQUE(usuario_id, categoria, inicio_periodo),
    CONSTRAINT chk_periodo_valido CHECK (fim_periodo > inicio_periodo)
);

-- Índices para orçamentos
CREATE INDEX idx_orcamentos_usuario ON orcamentos(usuario_id);
CREATE INDEX idx_orcamentos_categoria ON orcamentos(categoria);
CREATE INDEX idx_orcamentos_status ON orcamentos(status);
CREATE INDEX idx_orcamentos_periodo ON orcamentos(inicio_periodo, fim_periodo);

-- Tabela de metas financeiras
CREATE TABLE metas_financeiras (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    nome VARCHAR(255) NOT NULL,
    valor_alvo DECIMAL(15,2) NOT NULL CHECK (valor_alvo > 0),
    valor_atual DECIMAL(15,2) NOT NULL DEFAULT 0 CHECK (valor_atual >= 0),
    prazo DATE NOT NULL,
    tipo VARCHAR(30) NOT NULL CHECK (tipo IN ('EMERGENCIA', 'VIAGEM', 'COMPRA', 'INVESTIMENTO', 'OUTROS')),
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVA' CHECK (status IN ('ATIVA', 'CONCLUIDA', 'CANCELADA')),
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT chk_prazo_futuro CHECK (prazo > CURRENT_DATE),
    CONSTRAINT chk_valor_atual_menor_igual_alvo CHECK (valor_atual <= valor_alvo)
);

-- Índices para metas financeiras
CREATE INDEX idx_metas_usuario ON metas_financeiras(usuario_id);
CREATE INDEX idx_metas_status ON metas_financeiras(status);
CREATE INDEX idx_metas_prazo ON metas_financeiras(prazo);

-- Tabela de carteira de investimentos
CREATE TABLE investimentos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    ativo VARCHAR(20) NOT NULL,
    nome_ativo VARCHAR(255) NOT NULL,
    quantidade DECIMAL(15,6) NOT NULL CHECK (quantidade > 0),
    preco_compra DECIMAL(15,4) NOT NULL CHECK (preco_compra > 0),
    data_compra DATE NOT NULL,
    tipo_ativo VARCHAR(30) NOT NULL CHECK (tipo_ativo IN ('ACAO', 'FUNDO', 'RENDA_FIXA', 'CRIPTO', 'OUTROS')),
    ativo_registro BOOLEAN NOT NULL DEFAULT true,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Índices para investimentos
CREATE INDEX idx_investimentos_usuario ON investimentos(usuario_id);
CREATE INDEX idx_investimentos_ativo ON investimentos(ativo);
CREATE INDEX idx_investimentos_tipo ON investimentos(tipo_ativo);
CREATE INDEX idx_investimentos_ativo_registro ON investimentos(ativo_registro);

-- Tabela de insights da IA
CREATE TABLE insights_ia (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    tipo VARCHAR(50) NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT NOT NULL,
    justificativa TEXT NOT NULL,
    confirmado BOOLEAN NOT NULL DEFAULT false,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    valido_ate TIMESTAMP NOT NULL,
    
    CONSTRAINT chk_valido_ate_futuro CHECK (valido_ate > criado_em)
);

-- Índices para insights IA
CREATE INDEX idx_insights_usuario ON insights_ia(usuario_id);
CREATE INDEX idx_insights_tipo ON insights_ia(tipo);
CREATE INDEX idx_insights_confirmado ON insights_ia(confirmado);
CREATE INDEX idx_insights_valido_ate ON insights_ia(valido_ate);

-- Tabela de auditoria para transações
CREATE TABLE auditoria_transacoes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    transacao_id UUID NOT NULL REFERENCES transacoes(id),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    operacao VARCHAR(20) NOT NULL CHECK (operacao IN ('CREATE', 'UPDATE', 'DELETE')),
    dados_anteriores JSONB,
    dados_novos JSONB,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    ip_origem INET,
    user_agent TEXT
);

-- Índices para auditoria
CREATE INDEX idx_auditoria_transacao ON auditoria_transacoes(transacao_id);
CREATE INDEX idx_auditoria_usuario ON auditoria_transacoes(usuario_id);
CREATE INDEX idx_auditoria_operacao ON auditoria_transacoes(operacao);
CREATE INDEX idx_auditoria_data ON auditoria_transacoes(criado_em);

-- Função para atualizar timestamp de atualização
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.atualizado_em = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers para atualizar automaticamente o campo atualizado_em
CREATE TRIGGER update_transacoes_updated_at BEFORE UPDATE ON transacoes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orcamentos_updated_at BEFORE UPDATE ON orcamentos
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_metas_updated_at BEFORE UPDATE ON metas_financeiras
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_investimentos_updated_at BEFORE UPDATE ON investimentos
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();