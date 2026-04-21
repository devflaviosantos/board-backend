# Especificação de Funcionalidade: Backend do Quadro Kanban Pessoal

**Branch da Funcionalidade**: `001-board-backend-api`
**Criado em**: 2026-04-20
**Status**: Rascunho
**Entrada**: Descrição do usuário: "Backend de um sistema de quadro estilo Trello para uso pessoal com gerenciamento de Quadros, Colunas e Cartões via API REST"

## Cenários de Uso e Testes *(obrigatório)*

### História de Usuário 1 - Gerenciar Quadros (Prioridade: P1)

Como usuário pessoal, quero organizar meu trabalho em quadros nomeados para manter listas de tarefas separadas para diferentes projetos ou contextos.

**Por que esta prioridade**: Os quadros são o contêiner de nível superior — nada mais pode existir sem eles. Esta é a capacidade fundamental da qual todo o sistema depende.

**Teste Independente**: É possível criar um quadro, visualizá-lo na listagem, renomeá-lo e excluí-lo sem que nenhuma outra funcionalidade esteja implementada.

**Cenários de Aceite**:

1. **Dado** que não existem quadros, **Quando** crio um quadro com um nome válido, **Então** o quadro é salvo e retornado com um identificador único e data/hora de criação
2. **Dado** que existem quadros, **Quando** solicito a lista de todos os quadros, **Então** recebo todos os quadros com seus identificadores e nomes
3. **Dado** que um quadro existe, **Quando** o renomeio com um novo nome, **Então** o quadro é retornado com o nome atualizado
4. **Dado** que um quadro existe, **Quando** o excluo, **Então** ele é removido junto com todas as suas colunas e cartões
5. **Dado** que tento criar um quadro com nome em branco, **Quando** a requisição é enviada, **Então** ela é rejeitada com uma mensagem de erro descritiva

---

### História de Usuário 2 - Gerenciar Colunas Dentro de um Quadro (Prioridade: P2)

Como usuário pessoal, quero organizar um quadro em colunas nomeadas (ex: "A Fazer", "Em Andamento", "Concluído") para acompanhar visualmente o status das minhas tarefas ao longo dos estágios do fluxo de trabalho.

**Por que esta prioridade**: As colunas definem a estrutura do fluxo de trabalho dentro de um quadro. Elas são necessárias antes que qualquer cartão possa ser criado e conferem ao sistema sua natureza kanban.

**Teste Independente**: Dado que um quadro existe, é possível criar colunas, visualizá-las na ordem definida, renomeá-las, alterar sua ordem e excluí-las.

**Cenários de Aceite**:

1. **Dado** que um quadro existe, **Quando** adiciono uma coluna com um nome, **Então** a coluna é salva com uma posição atribuída automaticamente e retornada com seu identificador
2. **Dado** que um quadro possui colunas, **Quando** solicito suas colunas, **Então** recebo todas as colunas em ordem crescente de posição
3. **Dado** que uma coluna existe, **Quando** a renomeio ou altero seu valor de posição, **Então** a coluna atualizada é retornada refletindo as mudanças
4. **Dado** que uma coluna existe, **Quando** a excluo, **Então** ela é removida junto com todos os cartões que contém
5. **Dado** que tento criar uma coluna com nome em branco, **Quando** a requisição é enviada, **Então** ela é rejeitada com uma mensagem de erro descritiva

---

### História de Usuário 3 - Gerenciar Cartões Dentro de Colunas (Prioridade: P3)

Como usuário pessoal, quero criar e gerenciar cartões de tarefas dentro das colunas para rastrear itens de trabalho individuais com contexto como título, descrição, etiqueta e status de conclusão.

**Por que esta prioridade**: Os cartões são os principais itens de trabalho que o sistema rastreia. Criar, atualizar e excluir cartões é a interação de uso diário mais importante.

**Teste Independente**: Dado que um quadro com pelo menos uma coluna existe, é possível criar cartões com vários campos, atualizar seus detalhes, marcá-los como concluídos e excluí-los.

**Cenários de Aceite**:

1. **Dado** que uma coluna existe, **Quando** crio um cartão com título (e descrição e etiqueta opcionais), **Então** o cartão é salvo com uma posição atribuída automaticamente e retornado com seu identificador
2. **Dado** que um cartão existe, **Quando** atualizo seu título, descrição, etiqueta ou status de conclusão, **Então** o cartão atualizado é retornado refletindo as mudanças
3. **Dado** que um cartão existe, **Quando** o excluo, **Então** ele é permanentemente removido do sistema
4. **Dado** que tento criar um cartão com título em branco, **Quando** a requisição é enviada, **Então** ela é rejeitada com uma mensagem de erro descritiva

---

### História de Usuário 4 - Mover e Reordenar Cartões (Prioridade: P4)

Como usuário pessoal, quero arrastar cartões entre colunas e reordená-los para que o quadro reflita sempre o estado atual do meu trabalho.

**Por que esta prioridade**: Mover cartões é o que torna o sistema um quadro kanban dinâmico em vez de uma lista estática de tarefas. Isso viabiliza a interação de arrastar e soltar do frontend.

**Teste Independente**: Dado que um quadro com múltiplas colunas e cartões existe, é possível mover um cartão para uma coluna diferente e atualizar posições de cartões dentro de uma coluna.

**Cenários de Aceite**:

1. **Dado** que um cartão existe na coluna A, **Quando** atualizo o cartão com uma nova coluna destino e posição, **Então** o cartão é armazenado na coluna destino na posição especificada
2. **Dado** que múltiplos cartões existem em uma coluna, **Quando** atualizo o valor de posição de um cartão, **Então** o cartão é armazenado na nova posição conforme fornecido
3. **Dado** que solicito as colunas de um quadro, **Quando** a resposta é retornada, **Então** cada coluna inclui seus cartões ordenados pelo valor de posição

---

### Casos Extremos

- O que acontece quando um quadro é excluído? Todas as colunas e cartões dentro dele devem ser removidos automaticamente.
- O que acontece quando uma coluna é excluída? Todos os cartões dentro dela devem ser removidos automaticamente.
- O que acontece quando um título ou nome em branco é enviado para um quadro, coluna ou cartão? A requisição deve ser rejeitada com uma mensagem de erro clara e descritiva.
- Qual posição é atribuída a uma nova coluna? Ela recebe um valor de posição um maior que a maior posição atual entre as colunas do quadro.
- Qual posição é atribuída a um novo cartão? Ele recebe um valor de posição um maior que a maior posição atual entre os cartões da coluna.
- O que acontece quando o frontend envia uma nova posição para um cartão ou coluna durante reordenação/movimentação? O backend persiste o valor de posição fornecido como está; nenhuma compactação automática ou resolução de conflitos é aplicada.

## Requisitos *(obrigatório)*

### Requisitos Funcionais

- **RF-001**: O sistema DEVE permitir ao usuário criar quadros com um nome obrigatório
- **RF-002**: O sistema DEVE permitir ao usuário listar todos os quadros existentes
- **RF-003**: O sistema DEVE permitir ao usuário renomear um quadro existente
- **RF-004**: O sistema DEVE permitir ao usuário excluir um quadro, removendo automaticamente todas as suas colunas e cartões
- **RF-005**: O sistema DEVE permitir ao usuário criar colunas nomeadas dentro de um quadro
- **RF-006**: O sistema DEVE permitir ao usuário listar todas as colunas de um quadro em ordem crescente de posição, cada uma incluindo seus cartões ordenados por posição
- **RF-007**: O sistema DEVE permitir ao usuário renomear uma coluna ou atualizar seu valor de posição
- **RF-008**: O sistema DEVE permitir ao usuário excluir uma coluna, removendo automaticamente todos os seus cartões
- **RF-009**: O sistema DEVE permitir ao usuário criar cartões dentro de uma coluna, com título obrigatório e descrição e etiqueta opcionais
- **RF-010**: O sistema DEVE permitir ao usuário atualizar o título, descrição, etiqueta, posição, coluna de destino ou status de conclusão de um cartão
- **RF-011**: O sistema DEVE permitir ao usuário excluir um cartão
- **RF-012**: O sistema DEVE atribuir automaticamente a uma nova coluna o próximo valor de posição após a última coluna do quadro
- **RF-013**: O sistema DEVE atribuir automaticamente a um novo cartão o próximo valor de posição após o último cartão da coluna
- **RF-014**: O sistema DEVE rejeitar qualquer requisição de criação ou atualização onde um campo de nome ou título obrigatório esteja em branco, retornando uma mensagem de erro descritiva
- **RF-015**: O sistema DEVE aceitar operações de movimentação de cartão persistindo o identificador da coluna destino e o valor de posição fornecidos, sem forçar unicidade

### Entidades Principais

- **Quadro (Board)**: Um espaço de trabalho nomeado que agrupa colunas relacionadas. Possui um nome e uma data/hora de criação.
- **Coluna (Column)**: Um estágio de fluxo de trabalho nomeado dentro de um quadro (ex: "A Fazer", "Em Andamento"). Possui um nome e uma posição ordenada dentro do seu quadro. Pertence a exatamente um quadro.
- **Cartão (Card)**: Um item de tarefa dentro de uma coluna. Possui título obrigatório, descrição opcional, etiqueta de texto livre opcional (ex: "bug", "feature", "urgente"), uma posição ordenada dentro da sua coluna, um indicador booleano de conclusão e uma data/hora de criação. Pertence a exatamente uma coluna e pode ser movido entre colunas.

## Critérios de Sucesso *(obrigatório)*

### Resultados Mensuráveis

- **CS-001**: Todas as operações de dados para quadros, colunas e cartões são concluídas e retornam uma resposta confirmando o resultado sem recarregar a página
- **CS-002**: Ao solicitar as colunas de um quadro, elas são sempre retornadas em ordem crescente de posição, cada uma com seus cartões também em ordem crescente de posição
- **CS-003**: Toda submissão com um campo obrigatório em branco é rejeitada 100% das vezes com uma mensagem de erro legível identificando o campo inválido
- **CS-004**: Ao excluir um quadro, a remoção completa de todas as colunas e cartões associados é verificada por uma consulta subsequente que não retorna dados para o quadro excluído
- **CS-005**: Um cartão movido para uma coluna diferente aparece na coluna destino na posição especificada na próxima consulta
- **CS-006**: A API pode ser acessada por uma aplicação frontend em execução separada sem erros de conectividade

## Premissas

- O sistema é destinado a um único usuário pessoal e não requer autenticação ou gerenciamento de contas nesta versão
- Os dados não precisam persistir entre reinicializações do servidor na versão inicial; armazenamento em memória é aceitável para o MVP
- O gerenciamento de posições para reordenação por arrastar e soltar é feito pelo frontend; o backend aceita e persiste os valores de posição fornecidos pelo cliente sem forçar unicidade ou reordenação automática dos demais itens
- As etiquetas dos cartões são strings de texto livre (ex: "bug", "feature", "urgente") em vez de uma lista enumerada pré-definida
- O indicador de conclusão de um cartão é uma alternância simples; nenhuma transição de fluxo automática ou notificação é acionada por ele
- Uma única aplicação frontend consumirá esta API; o acesso de origem cruzada é configurado para permitir essa origem específica
- As respostas de erro incluem uma mensagem legível descrevendo qual campo falhou na validação e por quê
- Autenticação, suporte a múltiplos usuários, anexos de arquivos, notificações e conectividade a banco de dados externo estão explicitamente fora do escopo desta versão
