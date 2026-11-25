# Itens Lendários — projeto Paper

Projeto-fonte inicial para compilar um plugin Paper com três itens:

- **Tridente Z** — ao ser arremessado, aplica cegueira e lentidão por 5 segundos em jogadores atingidos.
- **Corno X** — empurra jogadores próximos em 360°, em até 15 blocos, sem causar dano; recarga de 10 segundos.
- **Pena W** — enquanto estiver na mão principal **ou** na segunda mão, mantém o jogador invisível sem partículas e oculta sua entidade para os demais jogadores (incluindo armadura e itens segurados).

## Comandos

- `/itenslendarios tridente [jogador]`
- `/itenslendarios corno [jogador]`
- `/itenslendarios pena [jogador]`

Permissão: `itenslendarios.admin` (padrão: operadores).

## Compilação

1. Abra o projeto em um ambiente com Gradle e JDK 25.
2. Execute `gradle build`.
3. O JAR será criado em `build/libs/ItensLendarios-1.0.0.jar`.
4. Envie esse JAR para a pasta `plugins` do servidor Paper.

## Observações importantes

- Este ZIP contém o **código-fonte**, não um JAR já compilado.
- O identificador da dependência Paper está em `gradle.properties`. Se o repositório Paper não publicar `26.3-R0.1-SNAPSHOT` com esse nome, será necessário ajustar `paperVersion` para o identificador exato da sua build.
- A Pena W oculta a entidade inteira para os outros jogadores para garantir que armadura e itens na mão também desapareçam. O jogador afetado ainda vê sua própria tela normalmente.
- A proteção contra destruição por jogadores e explosões ainda precisa ser implementada/testada para todos os eventos do servidor; este primeiro pacote estabelece a estrutura e as habilidades principais.
- A compatibilidade com Geyser depende da versão do servidor e deve ser testada no seu servidor real.
