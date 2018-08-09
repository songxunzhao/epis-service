# EPIS-service infrastructure

## Variables

- `environment` - tag on resources, default `csd`

- `client_id` - OAuth client id

- `client_secret`- OAuth client secret

- `token_info_uri` - OAuth token info URI

- `jdbc_database_url` - Database URL

- `jdbc_database_username` - Database username

- `jdbc_database_password` - Database password

- `mhub_keystore_part1` - First part of mhub keystore

- `mhub_keystore_part2` - Second part of mhub keystore

- `mhub_keystore_password` - Password for MHUB keystore
  
- `mhub_password` - Password for MHUB
  
- `mhub_userid` - UserID for MHUB
  
- `mq_host` - MQ host name
  
- `mq_password` - MQ password
  
- `mq_username` - MQ username
  
- `rollbar_access_token` - Access token for Rollbar service
  
- `spring_profiles_active` - Active spring profile (`production`)
  
## Requirements

- VPC and subnet with `Environment` tag with value `csd`

### Keystore

To create a keystore for MHUB you need to add keys to a p12 keystore and then convert it to base64.
`cat keystore.p12 | base64 > keystore_in_base64`

To decode the keystore use
`cat keystore_in_base64 | base64 --decode > keystore.p12`

**NB**
AWS doesn't enable env vars bigger than 4096 characters, so we need to split up the keystore:
`split --bytes=4096 keystore /path/to/splits`
