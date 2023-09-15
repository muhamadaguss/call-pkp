
# CALL PKP MODULE

Library for module call asterisk with linphone-sdk

## Documentation

- openjdk 17


# Build

```bash
build.gradle (Groovy)

1. Add it in your root build.gradle at the end of repositories:

allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

2. Add the dependency

dependencies {
	        implementation 'com.github.muhamadaguss:call-pkp:Tag'
	}

build.gradle.kts

1. Add it in your setting.gradle.kts at the end repositories:

dependencyResolutionManagement {
    repositories {
        ...
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

2. Add the dependency

implementation("com.github.muhamadaguss:call-pkp:Tag")
```


