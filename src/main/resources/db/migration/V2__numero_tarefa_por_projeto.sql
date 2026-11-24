-- 1. Colunas novas. Nascem permissivas para nao quebrar as linhas que ja existem.
ALTER TABLE tarefas  ADD COLUMN numero_no_projeto integer;
ALTER TABLE projetos ADD COLUMN proximo_numero_tarefa integer NOT NULL DEFAULT 1;

-- 2. Backfill: numera as tarefas existentes de cada projeto (1, 2, 3...)
--    na ordem em que foram criadas (id crescente).
UPDATE tarefas t
SET numero_no_projeto = r.rn
    FROM (SELECT id,ROW_NUMBER() OVER (PARTITION BY projeto_id ORDER BY id) AS rn FROM tarefas) r
WHERE t.id = r.id;

-- 3. Inicializa o contador de cada projeto: maior numero existente + 1.
--    Projeto sem tarefas fica em 1 (COALESCE cobre o MAX nulo).
UPDATE projetos p
SET proximo_numero_tarefa = COALESCE((SELECT MAX(t.numero_no_projeto) FROM tarefas t WHERE t.projeto_id = p.id), 0) + 1;

-- 4. Agora todo registro tem numero: aplica as regras definitivas.
ALTER TABLE tarefas ALTER COLUMN numero_no_projeto SET NOT NULL;
ALTER TABLE tarefas
    ADD CONSTRAINT uk_numero_tarefa_projeto UNIQUE (projeto_id, numero_no_projeto);