# user-session-backend
A Spring backend that exposes a GraphQL interface that allows user-management.

## Setup
- pull this project
- navigate into `docu/docker` and run `docker-compose up -d` to create and start all required containers for this project (DB, ...)


## Example queries:
##### register a user:

```graphql
query register {
    registerUser(userDto: {
			firstName: "first",
			lastName: "last",
			username: "username",
			email: "user@mail.com"
		}, password: "pw")
}
```

##### login:

```graphql
query login {
    authenticateUser(email: "user@mail.com", password: "pw")
}
```

##### update user information:

```graphql
query updateUser {
	updateUser(sessionToken: "0PDuouskqkS9G0mwqd4Pd", password: "pw", userDto: {
		firstName: "newFirst",
		username: "newuserName"
	})
}
```

##### find a user by id:

```graphql
query findUser {
	findUserById(userId: 1) {
		username,
		email
	}
}
```
