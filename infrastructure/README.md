# EPIS-service infrastructure

## Variables

- `environment` - tag on resources, default `csd`

Other parameters (secrets) will be read from AWS SSM.
  
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
