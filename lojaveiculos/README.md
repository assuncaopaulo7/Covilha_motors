Isto é um exemplo de uma página simples, do início ao fim.
Comecei por registar cliente, depois registar vendedor, pôr carros à venda usando o vendedor, e comprar carros usando o cliente.
E também a funcionalidade de Adicionar, Atualizar e Eliminar carros pelo vendedor, e Comprar carros pelo cliente.
Gerar faturas. Erros e mensagens de aviso.

Vou meter o código no github para darem uma vista de olhos. As tabelas estao muito simples ainda, e não sei como fazer com q um de nós corra o projeto num computador e outro colega aceda ao website (usando o mesmo wifi) e possamos simular:
a) Pessoa 1 corre website
b) Pessoa 2 acede ao website e acede como vendedor para gerir carros. E depois aceder como segundo vendedor (Podem haver vários cliente e vendedores)
c) Pessoa 3 acede ao website e acede como cliente para comprar carros
d) Pessoa 4 acede ao website e acede como segundo cliente para comprar carros

!!! PARA TESTAREM NOS VOSSOS PC'S !!!:
1) clicar no canto direito do inteil no "m" de "Maven" e clicar no reload Icon e escolher "Reload all Projects"
2) Dar run no play button
3) ir ao website "localhost:8080"

(O localhost:3306 é para a conexao no mysql. Corresponde ao sítio onde temos a base de dados)

.......

Privacy & Security > Location no windows faz com q possamos connectar do smartphone para o servidor do vosso pc q esta a correr o codigo

No vosso pc ir ao CMD e escrever "ipconfig" e vao ver uma coisa deste genero:

IPv4 Address. . . . . . . . . . . : 192.168.1.2

Ir ao browser do vosso smartphone e escrever:

http://192.168.1.2:8080/

......

Passar os "vendedores" para "gestores".

Cada vez que os gestores meterem um carro para dentro da tabela de carros, deve haver uma outra tabela que dica:
("Atualizar" button, "Eliminar" button e "Adicionar novo" sao ambos INSERIU/RETIROU 


TABLE

gestor_id  car_id  inserir/retirar  number_of_cars

1          4       INSERIU          1

2          4       RETIROU          2

......

Marcos:
TO_DO_LIST: Temos de ter um botao no website chamado "estatísticas" onde podemos ver os users todos, mas podemos filtrar por "maior gastador" ou "menor gastador" ou outros fatores importantes, ou podemos ver todos os carros e filtrar pelo modelo e/ou categoria "mais vendido" ou pelo modelo e/ou categoria que gerou mais dinheiro" ou o "último modelo que deu entrada no website pelo vendedor". E outras estatísticas como "SUMs", "AVGs" e "COUNTs" (soma, média de profits e counts de vendas, etc)


TO_DO_LIST: Column "Disponibilidade" na tabela Car, que podem dizer "Disponível" ou "Esgotado"

