

# Descrição e passo a passo da criação de uma simples API de vacinas em Spring Java


## Sumário

- 1.0 Tecnologias utilizadas
- 2.0 Definindo um plano de desenvolvimento para nossa aplicação
- 3 .0 Criando a base de dados
- 4.0 Definindo as entidades da base de dados para o Hibernate
- 5.0 Utilizando o Spring Data JPA para acessar operar ações na nossa base de dados.
- dados dos Usuários e das Vacinas. 6.0 Criando uma camada de serviço para comunicação com os Objetos de Acesso de
- 7.0 Implementando o nosso Rest Controller para a comunicação dos pedidos HTTP
   - 7.1 Lidando com maus cadastros e erros na aplicação
      - 7.1.1 Criando um Controller Advice para lidar com as exceções mandadas ao sistema
   - 7.2 Criando nosso Rest Controller
      - 7.2.1 Criando o endpoint /usuarios para o cadastro de usuários
      - 7.2.2 Criando e implementando o endpoint /vacinas para o cadastro de vacinas.
- 8.0 Testando nossa REST API com o Postman (Coleção de imagens)
- 9.0 Usando um método temporário GET para ver o estado da nossa base de dados


## 1.0 Tecnologias utilizadas

Spring Boot: Com o Spring Boot, podemos começar nossa REST API mais rapidamente,
pois essa tecnologia facilita na criação do projeto, além de organizar todas as dependências
Maven que precisaremos no projeto. Além disso, a configuração do projeto Spring será
reduzida de maneira significante, diminuindo a chance de bugs/erros devidos a uma
configuração mal implementada.

Spring Web: A dependência Spring Web disponibilizada no Spring Boot nos permitirá
desenvolver a aplicação REST, com a biblioteca Spring convertendo nossos Objetos Java
para o formato JSON para nossa API, com um servidor já embutido na nossa aplicação.
Ainda mais, criando um novo Bean do tipo @RestController, a biblioteca lidará com os
pedidos e respostas REST, ajudando em uma boa parte do back-end deste projeto.

Hibernate: Usaremos o Hibernate para lidar com as operações low-level SLQ na nossa
aplicação, diminuindo a quantidade de código JDBC que teremos que escrever, acelerando
o desenvolvimento e comunicação da nossa aplicação com a base de dados.

MySQL: Para a criação da nossa base de dados, usaremos o MySQL Community Server,
pois além de ser gratuito, é uma database open source popular com uma grande
comunidade.

Spring Data JPA: Utilizaremos a JPA API para a comunicação do Hibernate e nossa
aplicação, faremos uso da especificação JPA ao invés da linguagem nativa do Hibernate,
pois dessa forma, nosso código será mais portável e flexível, e caso seja necessário no
futuro, será possível trocar do Hibernate para outra tecnologia com poucas mudanças no
código. Além disso, com o Spring Data JPA, poderemos diminuir muito nosso código fonte,
pois essa já nos disponibiliza todos os métodos básicos para aquisição de dados da
database com apenas uma implementação básica de interface (JpaRepository<Objeto, Id>).

Spring Boot DevTools: Usaremos essa tecnologia para acelerar nosso desenvolvimento,
com a tecnologia de auto-refresh e reinicialização mais rápida da aplicação que essa
disponibiliza.

Hibernate Validator: Essa tecnologia nos ajudará na validação do CPF, e-mail e nome
(nome longo demais) das vacinas e dos usuários.

Postman: Usaremos a aplicação Postman para facilitar nossos testes dos cadastros na
API.

## 2.0 Definindo um plano de desenvolvimento para nossa aplicação

Antes de começar a aplicação, devemos inicialmente criar um plano de desenvolvimento
para nossa REST API:

1. Criar uma base de dados


2. Criar as classes correspondentes às tabelas na base de dados para a comunicação com
    o Hibernate
3. Desenvolver Objetos de Acesso de Dados para disponibilizar as operações CRUD ao
    nosso aplicativo no ambiente Java.
4. Implementar uma camada de serviço para a comunicação do nosso controlador Rest e
    os OAD
5. Criar nosso RestController para lidar com os pedidos HTTP
6. Criar endpoints na nossa REST API para o cadastro tanto de usuários tanto de vacinas
7. Receber pedidos HTTP POST e cadastrar diretamente na nossa base de dados com
    nossa camada de serviço
8. Criar um objeto RestResponse para comunicar as respostas dos servidores e seus
    motivos devidos
9. Lidar com erros no preenchimento de dados e enviar uma resposta adequada

Para esse plano de desenvolvimento, precisaremos:

- 2 Entidades: User, Vaccine para mapear a base de dados ao Hibernate;
- 2 Objetos de Acesso de dados UserRepository, VaccineRepository para
    disponibilizar as operações CRUD à nossa aplicação;
- Uma interface de serviço UserVaccineService e sua implementação
    UserVaccineServiceImpl
- Um Controlador Rest para lidar com os pedidos HTTP à nossa API;
- Um Objeto para as respostas Rest aos pedidos HTTP, RestResponse;
- Classes de erros personalizadas para lançar exceções ao sistema caso haja um erro
    de cadastro;
- Um objeto do tipo ControllerAdvice para lidar com essas exceções

## 3 .0 Criando a base de dados

Primeiramente, criaremos a base de dados no MySQL, de acordo com as especificações
indicadas:

```sql
create table `user` (
    `name` varchar(128) not null,
    `cpf` varchar(14) not null,
    `email` varchar(60) not null,
    `birth_date` date not null,
    
    unique key (`cpf`),
    unique key (`email`),
    
    primary key(`cpf`)
) engine=InnoDB default charset=utf8;

create table `vaccine` (
    `id` int8 not null auto_increment,
    `name` varchar(128) not null,
    `date` date not null,
    `user_cpf` varchar(14) not null,
    primary key(`id`),
    key `FK_USER_idx` (`user_cpf`),
    constraint `FK_USER` foreign key (`user_cpf`)
    references `user` (`cpf`) 
    on delete no action on update no action
) engine=InnoDB auto_increment=100 default charset=utf8;
```

Definimos os campos como não nulos, pois a especificação deseja que os dados sejam
obrigatórios, e criamos uma relação One to Many entre a base de dados user e vaccine,
que iremos configurar pelo código Java, por isso a opção “on delete no action on update
no action” foi adicionada. O CPF será usado como um id primário do usuário, pois esse é
único.

## 4.0 Definindo as entidades da base de dados para o Hibernate

Após criar a base de dados no MySQL, precisamos criar as entidades para o Usuário e as
Vacinas:

```java
@Entity
@Table(name="user")
public class User {
    @NotNull
    @Column(name="name")
    private String name;

    @CPF
    @NotNull
    @Id
    @Column(name="cpf")
    private String cpf;

    @Email
    @NotNull
    @Column(name="email")
    private String email;

    @Column(name="birth_date")
    @NotNull
    private Date birthDate;

    @OneToMany(mappedBy="user",
            cascade=CascadeType.ALL)
    @JsonManagedReference // Evita recursão infinita ao serializar para json.
    // Quando um usuário for deletado, queremos deletar todas as vacinas relacionadas
    List<Vaccine> vaccines;

    // Construtores padrões e getters/setters ...

    public User() {
    }

    public User(@NotNull String name, @CPF @NotNull String cpf,
                @Email @NotNull String email, @NotNull Date birthDate) {
        this.name = name;
        this.cpf = cpf;
        this.email = email;
        this.birthDate = birthDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public List<Vaccine> getVaccines() {
        return vaccines;
    }

    public void setVaccines(List<Vaccine> vaccines) {
        this.vaccines = vaccines;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", cpf='" + cpf + '\'' +
                ", email='" + email + '\'' +
                ", birthDate=" + birthDate +
                '}';
    }
}
```

```java
@Entity
@Table(name="vaccine")
public class Vaccine {
    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name")
    @NotNull
    private String name;

    @Column(name="date")
    @NotNull
    private Date date;

    @ManyToOne(cascade={
            CascadeType.PERSIST,
            CascadeType.MERGE,
            CascadeType.DETACH,
            CascadeType.REFRESH
    })
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @JoinColumn(name="user_cpf")
    @JsonBackReference // Evita recursão infinita ao serializar para json.
    @NotNull
    // Não queremos que o usuário seja deletado se a vacina for deletada
    private User user;

    // Construtores padrões e getters/setters...

    public Vaccine() {
    }

    public Vaccine(String name, Date date, User user) {
        this.name = name;
        this.date = date;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
```
Colocamos a anotação @NotNull nos campos obrigatórios, e utilizamos a biblioteca do
Hibernate Validator para validar nosso campo de CPF, além de definir a relação entre o
usuário e suas vacinas, utilizando as anotações @OneToMany e @ManyToOne. Como
uma vacina sem recebedor não faz sentido, indicamos na nossa especificação para deletar
todas as vacinas do usuário se esse for deletado.

Além disso, para formatar as datas no formato padrão brasileiro dia/mês/ano, tivemos que
colocar as seguintes configurações no application.properties para funcionar
corretamente:

```properties
# Jackson properties
spring.jackson.date-format=dd/MM/yyyy
# Evita a conversão de datas
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.time-zone=America/Sao_Paulo

# Date-format serialization
spring.gson.date-format=dd/MM/yyyy
```

## 5.0 Utilizando o Spring Data JPA para acessar operar ações na nossa base de dados.

Agora, utilizaremos a biblioteca Spring Data JPA para facilitar a nossa criação do Objeto
de acesso de dados.

Com a implementação abaixo, podemos reduzir muito a quantidade de código que deve ser
escrito, além disso, a implementação já nos dá o que precisamos da base de dados, que é
cadastrar usuários e vacinas.

```java
public interface UserRepository extends JpaRepository<User, String> {
    // Vamos usar essas query para adicionar vacinas a partir do email ou cpf do usuário no futuro
    @Query("from User where cpf=:cpf")
    User getUserByCpf(@Param("cpf") String cpf);

    @Query("from User where email=:email")
    User getUserByEmail(@Param("email") String email);
}
```

```java
public interface VaccineRepository extends JpaRepository<Vaccine, Long> {
}
```

Se fizéssemos uma implementação típica, teríamos que criar duas interfaces para os
Objetos de Acesso de Dados e suas duas implementações, além de muitas linhas de código
criando métodos para o acesso à base de dados, construtores, anotações e entre outros.

Porém, ao utilizar o Spring Data JPA, todo esse trabalho pode ser reduzido para poucas
linhas de códigos, nos dando as implementações CRUD de forma simples e rápida.

Além disso, a criação dos métodos de pesquisa de usuário foi feita de forma muito simples
e prática, e precisaremos dela no futuro para a adição de vacinas na base de dados.


6.0 Criando uma camada de serviço para comunicação com os Objetos de Acesso de
dados dos Usuários e das Vacinas.

Seguindo o paradigma Service Layer design pattern, vamos criar um único serviço para
implementar as ações de cadastrar usuários e vacinas, que iremos usar depois no nosso
controlador Rest.

```java
@Service
public class UserVaccineServiceImpl implements UserVaccineService {

    private final UserRepository userRepository;

    private final VaccineRepository vaccineRepository;

    @Autowired
    public UserVaccineServiceImpl(UserRepository userRepository,
                                  VaccineRepository vaccineRepository) {
        this.userRepository = userRepository;
        this.vaccineRepository = vaccineRepository;
    }

    @Override
    public Vaccine registerVaccine(Vaccine vaccine) {
        // Garante que irá registrar uma nova vacina settando id para 0
        vaccine.setId(0L);
        return vaccineRepository.save(vaccine);
    }

    @Override
    public User registerUser(User user) {
        if(userRepository.findById(user.getCpf()).isPresent())
            throw new DuplicateKeyException("Usuário com cpf já encontrado");
        else
            return userRepository.save(user);
    }

    @Override
    public User getUserByCpf(String cpf) {
        return userRepository.getUserByCpf(cpf);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.getUserByEmail(email);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
```

## 7.0 Implementando o nosso Rest Controller para a comunicação dos pedidos HTTP

Agora, nós precisamos criar nosso Bean do tipo RestController, para podermos fazer a
comunicação da nossa aplicação com os pedidos HTTP à nossa REST API.


Criaremos dois endpoints

- /usuarios – Receberá os pedidos HTTP POST para o cadastro do usuário, no formato
    JSON.
- /vacinas – Receberá os pedidos HTTP POST para o cadastro das vacinas, com o e-
    mail ou CPF do usuário para o cadastro.

Para as respostas do sistema, além de enviarmos um código de status 201 para cadastro
feitos com sucesso, e 400 para cadastros com dados inválidos, enviaremos uma resposta
em JSON indicando o status de retorno e uma mensagem do sistema, para caso o cliente
envie um cadastro inválido, podermos enviar um feedback desse erro pela nossa API.

Para isso, iremos criar um objeto simples RestResponse, que será usado para enviar essas
mensagens ao cliente:

```java
public class RestResponse {
    private int status;

    private String message;

    public RestResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
```

### 7.1 Lidando com maus cadastros e erros na aplicação

Maus HTTP requests possíveis:


1. Cadastro de vacina com um usuário que não existe na base de dados, para isso
    criaremos a classe UserNotFoundException, para mandar essa exceção para o
    sistema sempre que esse tipo de erro for feito
2. Má formatação de dados. Para o caso do cliente da nossa API mande dados
    incorretamente, como um CPF ou e-mail inválido, criaremos a classe
    InvalidFormException, mandando para nossa aplicação esse erro quando o
    cadastro tiver uma forma inválida.
3. Recadastro de usuário já existente. No caso de o cliente tentar cadastrar um novo
    usuário com um CPF ou e-mail já existente na base de dados, enviaremos um
    DuplicateKeyException para nosso sistema.
4. Outro erro genérico post. Em casos onde há um erro genérico nos cadastros,
    enviaremos uma simples mensagem ao cliente indicando que houve um erro.

#### 7.1.1 Criando um Controller Advice para lidar com as exceções mandadas ao sistema

Para poder gerenciar as exceções lançadas no nosso aplicativo, iremos criar um Controller
Advice. A partir dele, iremos controlar todo o Exception Handling do nosso RestController,
e enviaremos o status 400 junto com um feedback do erro sempre que ocorrer um erro.

A implementação de tal Controller Advice será feita da forma seguinte:
```java
@ControllerAdvice
public class VaccineApiErrorHandler {
    @ExceptionHandler
    public ResponseEntity<RestResponse> invalidFormHandler(InvalidFormException e) {
        e.printStackTrace();
        return new ResponseEntity<>(
                new RestResponse(HttpStatus.BAD_REQUEST.value(),
                        "Dados de cadastro inválidos: " + e.getInvalidValue()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler
    public ResponseEntity<RestResponse> duplicateCpfHandler(DuplicateKeyException e) {
        e.printStackTrace();
        return new ResponseEntity<>(
                new RestResponse(HttpStatus.BAD_REQUEST.value(),
                        e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler ResponseEntity<RestResponse> userNotFoundHandler(UserNotFoundException e) {
        return new ResponseEntity<>(
                new RestResponse(HttpStatus.BAD_REQUEST.value(),
                        e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler
    public ResponseEntity<RestResponse> badRequestHandler(Exception e) {
        e.printStackTrace();
        return new ResponseEntity<>(
                new RestResponse(HttpStatus.BAD_REQUEST.value(), "Dados de cadastro inválidos"),
                HttpStatus.BAD_REQUEST
        );
    }
}
```

```
Criamos diferentes métodos para lidar com os diversos erros devido a mal cadastros na
API, e enviamos diferentes feedbacks, para o cliente poder compreender os erros nas
suas chamadas à nossa API.
```
### 7.2 Criando nosso Rest Controller

Agora que já criamos nosso serviço para acessar a base de dados, as classes de erro, e
o Controller Advice para lidar com os ditos erros, podemos finalmente criar nosso
RestController.


#### 7.2.1 Criando o endpoint /usuarios para o cadastro de usuários

Primeiramente criaremos o endpoint /usuarios, cuja implementação pode ser vista a
seguir:
```java
@RestController
public class VaccineApiRestController {

    private final UserVaccineService service;

    @Autowired
    public VaccineApiRestController(UserVaccineService service) {
        this.service = service;
    }

    @PostMapping("usuarios")
    public ResponseEntity<RestResponse> registerUser(
            @Valid @RequestBody User user, BindingResult result) {
        verifyRegisterData(result); // Se houver dados mal formatados, lança um InvalidFormException
        service.registerUser(user);

        return new ResponseEntity<>(
                new RestResponse(HttpStatus.CREATED.value(), "Usuário cadastrado com sucesso!"),
                HttpStatus.CREATED
        );
    }
}
```
Podemos ver na primeira linha de código do método para cadastrar o usuário, “registerUser”,
usamos um método “verifyRegisterData” que utiliza o resultado da validação e vinculação
dos dados para verificar se houve erros nessas validações, e caso haja algo incorreto, é
lançado uma exceção ao sistema.

Caso a validação for correta, registraremos o usuário logo em seguida, mas se houver outro
problema, como falta de dados no cadastro, ou um recadastro de usuário, enviaremos ou
uma exceção genérica Exception ou uma DuplicateKeyException. Para a verificação do
resultado da validação, utilizamos o seguinte método, dentro do nosso Rest Controller:

```java
private void verifyRegisterData(BindingResult result) throws InvalidFormException {
    if (result.hasErrors()) {
        for (FieldError error : result.getFieldErrors()) {
            throw new InvalidFormException("Dados de cadastro inválidos.", error.getField());
        }
    }
}
```

#### 7.2.2 Criando e implementando o endpoint /vacinas para o cadastro de vacinas.

Agora, só falta criarmos o endpoint para o cadastro de vacinas. Precisaremos de um
método que receba um HTTP POST com os dados do nome da vacina, data que a vacina
foi aplicada, e o e-mail/CPF do usuário, para fazer esse cadastro.


Para isso criaremos primeiro um método para extrair um usuário da base dados a partir
do e-mail inscrito no POST ou o CPF, descrito a seguir:

```java
private User getUserFromDatabase(User user) {
      if(user == null) return null;

      if(user.getCpf() != null) {
          user = service.getUserByCpf(user.getCpf());
      } else if(user.getEmail() != null) {
          user = service.getUserByEmail(user.getEmail());
      }
      return user;
  }
```

Com isso, poderemos recuperar o usuário da base de dados com apenas o valor do CPF
ou e-mail enviados por nossos clientes. No caso de não encontrarmos usuário (retorno
nulo) poderemos simplesmente lançar um UserNotFoundException e deixar o nosso
ControllerAdvice lidar com nossa exceção lançada.

Com as considerações feitas, agora podemos implementar nosso método para receber os
pedidos posts para o novo endpoint:

```java
@PostMapping("vacinas")
public ResponseEntity<RestResponse> registerVaccine(
        @Valid @RequestBody Vaccine vaccine, BindingResult result) {
    verifyRegisterData(result); // Se houver dados mal formatados, lança um InvalidFormException
    User user = getUserFromDatabase(vaccine.getUser());
    if(user == null) throw new UserNotFoundException("Usuário não encontrado na base de dados");

    vaccine.setUser(user);
    service.registerVaccine(vaccine);
    return new ResponseEntity<>(
            new RestResponse(HttpStatus.CREATED.value(), "Vacina cadastrada com sucesso!"),
            HttpStatus.CREATED
    );
}
```

Após isso, nosso controler estará pronto:

```java
@RestController
public class VaccineApiRestController {

    private final UserVaccineService service;

    @Autowired
    public VaccineApiRestController(UserVaccineService service) {
        this.service = service;
    }

    @PostMapping("usuarios")
    public ResponseEntity<RestResponse> registerUser(
            @Valid @RequestBody User user, BindingResult result) {
        verifyRegisterData(result); // Se houver dados mal formatados, lança um InvalidFormException
        service.registerUser(user);

        return new ResponseEntity<>(
                new RestResponse(HttpStatus.CREATED.value(), "Usuário cadastrado com sucesso!"),
                HttpStatus.CREATED
        );
    }

    @PostMapping("vacinas")
    public ResponseEntity<RestResponse> registerVaccine(
            @Valid @RequestBody Vaccine vaccine, BindingResult result) {
        verifyRegisterData(result); // Se houver dados mal formatados, lança um InvalidFormException
        User user = getUserFromDatabase(vaccine.getUser());
        if(user == null) throw new UserNotFoundException("Usuário não encontrado na base de dados");

        vaccine.setUser(user);
        service.registerVaccine(vaccine);
        return new ResponseEntity<>(
                new RestResponse(HttpStatus.CREATED.value(), "Vacina cadastrada com sucesso!"),
                HttpStatus.CREATED
        );
    }

    private void verifyRegisterData(BindingResult result) throws InvalidFormException {
        if (result.hasErrors()) {
            for (FieldError error : result.getFieldErrors()) {
                throw new InvalidFormException("Dados de cadastro inválidos.", error.getField());
            }
        }
    }

    private User getUserFromDatabase(User user) {
        if(user == null) return null;

        if(user.getCpf() != null) {
            user = service.getUserByCpf(user.getCpf());
        } else if(user.getEmail() != null) {
            user = service.getUserByEmail(user.getEmail());
        }
        return user;
    }
}
```

Pronto! Nossa aplicação REST API está completa, tudo que falta agora é testa-la na web
com o Postman, para vermos se ela está respondendo de forma correta:
