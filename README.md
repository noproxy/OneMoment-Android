## Import

### local properties

You must define those

    sdk.dir=/path/to/your/Android/sdk
    KEY_ALIAS_RELEASE=example
    KEY_ALIAS_DEBUG=example
    STORE_PASSWORD=password
    KEY_PASSWORD=password
    KEYSTORE_PATH=/path/to/keystore/file

in your ```local.properties``` file in project root directory.


## Gitignore

File [.gitignore](.gitignore) has some contains necessary rules. Please keep they and add some rules if you use different environment.

- You should ignore all build and generated files.
- You should ignore all your IDE configure and environment-dependent files.
- You should ignore file that contains private information, such as Bintray API key, Github access token.


## Format

- Only use UTF-8 encoding file
- Only use LF line break
- Never use Non-ASCII character except resource file

## Style

Apply code style from  https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml.

Step: 

 Download the intellij-java-google-style.xml file from the http://code.google.com/p/google-styleguide/ repo.
 Copy it into your config/codestyles folder in your intellij/android-studio settings folder. Then restart IDE.
 Under Settings/Code Style select the google-styleguide as current code style.
    



























.




