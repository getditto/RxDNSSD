# 📣 Android mDNSResponder [![Download](https://img.shields.io/maven-central/v/live.ditto/dnssd?label=DNSSD)](https://search.maven.org/artifact/live.ditto/dnssd) [![Download](https://img.shields.io/maven-central/v/live.ditto/rxdnssd?label=RxDNSSD)](https://search.maven.org/artifact/live.ditto/rxdnssd) [![Download](https://img.shields.io/maven-central/v/live.ditto/rx2dnssd?label=Rx2DNSSD)](https://search.maven.org/artifact/live.ditto/rx2dnssd)

## 🍴 Ditto Fork of RxDNSSD

Welcome to the [Ditto](https://www.ditto.live/) fork of RxDNSSD. The original [RxDNSSD](https://github.com/andriydruk/RxDNSSD) library was created by [Andriy Druk](https://andriydruk.com/). Andriy is unable to continue supporting the library due to the Russia's unprovoked war in Ukraine. We are grateful that Andriy was able to build this amazing library! You can read his [explanation](https://andriydruk.com/post/mdnsresponder/) about why jmDNS, Android NSD Services and Google Nearby API are not good enough, and why he built this library.

We created this fork because the Ditto Android SDK uses this library for mDNS functionality to discover other small peers on the same WiFi LAN. The maven coordinates for releases from this repo are similar, but have a groupId of `live.ditto`.

- `com.github.andriydruk:*dnssd:0.9.14-0.9.17`
- `live.ditto:*dnssd:1.0.0+`

## Hierarchy

There are two versions of mDNSReponder.

Bindable version:

```
                                   +--------------------+       +--------------------+
                                   |      RxDNSSD       |       |       Rx2DNSSD     |
                                   +--------------------+       +--------------------+
                                           |                            |
                                           |   +--------------------+   |
                                            -->| Android Java DNSSD |<--
                                               +--------------------+
                                               |  Apple Java DNSSD  |
                 +------------------+          +--------------------+
                 |    daemon.c      |<-------->|     mDNS Client    |
                 +------------------+          +--------------------+
                 |    mDNS Core     |
                 +------------------+
                 | Platform Support |
                 +------------------+
                    System process                Your Android app

```

Embedded version:

```
                     +--------------------+       +--------------------+
                     |      RxDNSSD       |       |       Rx2DNSSD     |
                     +--------------------+       +--------------------+
                                |                            |
                                |   +--------------------+   |
                                 -->| Android Java DNSSD |<--
                                    +--------------------+
                                    |   Apple Java DNSSD |
                                    +--------------------+
                                    |    mDNS Client     |
                                    +--------------------+
                                    | Embedded mDNS Core |
                                    +--------------------+
                                    | Platform Support   |
                                    +--------------------+
                                      Your Android app

```

## Binaries on MavenCentral

DNSSD library:

```groovy
compile 'live.ditto:dnssd:1.0.0'
```

RxDNSSD library:

```groovy
compile 'live.ditto:rxdnssd:1.0.0'
```

Rx2DNSSD library:

```groovy
compile 'live.ditto:rx2dnssd:1.0.0'
```

* It's built with Android NDK 24 for all platforms (1.7 MB). If you prefer another NDK version or subset of platforms, please build it from source with command:

```command
./gradlew clean build
```

## How to use

### DNSSD

Dnssd library provides two implementations of DNSSD interface:

`DNSSDBindable` is an implementation of DNSSD with system's daemon. Use it for Android project with min API higher than 4.1 for an economy of battery consumption (Also some Samsung devices can don't work with this implementation).

```java
DNSSD dnssd = new DNSSDBindable(context);
```

DNSSDEmbedded is an implementation of RxDnssd with embedded DNS-SD core. Can be used for any Android device with min API higher than Android 4.0.

```java
DNSSD dnssd = new DNSSDEmbedded();
```

##### Register service
```java
try {
	registerService = dnssd.register("service_name", "_rxdnssd._tcp", 123,
   		new RegisterListener() {

			@Override
			public void serviceRegistered(DNSSDRegistration registration, int flags,
				String serviceName, String regType, String domain) {
				Log.i("TAG", "Register successfully ");
			}

			@Override
         	public void operationFailed(DNSSDService service, int errorCode) {
				Log.e("TAG", "error " + errorCode);
        	}
   		});
} catch (DNSSDException e) {
	Log.e("TAG", "error", e);
}
```

##### Browse services example

```java
try {
	browseService = dnssd.browse("_rxdnssd._tcp", new BrowseListener() {

 		@Override
		public void serviceFound(DNSSDService browser, int flags, int ifIndex,
			final String serviceName, String regType, String domain) {
			Log.i("TAG", "Found " + serviceName);
		}

		@Override
		public void serviceLost(DNSSDService browser, int flags, int ifIndex,
			String serviceName, String regType, String domain) {
			Log.i("TAG", "Lost " + serviceName);
		}

		@Override
		public void operationFailed(DNSSDService service, int errorCode) {
			Log.e("TAG", "error: " + errorCode);
		}
	});
} catch (DNSSDException e) {
	Log.e("TAG", "error", e);
}
```

You can find more samples in app inside this repository.

### RxDNSSD

- RxDnssdBindable
```
RxDnssd rxdnssd = new RxDnssdBindable(context);
```
- RxDnssdEmbedded
```
RxDnssd rxdnssd = new RxDnssdEmbedded();
```

##### Register service

```java
BonjourService bs = new BonjourService.Builder(0, 0, Build.DEVICE, "_rxdnssd._tcp", null).port(123).build();
Subscription subscription = rxdnssd.register(bonjourService)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(service -> {
      		updateUi();
      }, throwable -> {
        	Log.e("DNSSD", "Error: ", throwable);
      });
```

##### Browse services example

```java
Subscription subscription = rxDnssd.browse("_http._tcp", "local.")
	.compose(rxDnssd.resolve())
    .compose(rxDnssd.queryRecords())
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(new Action1<BonjourService>() {
    	@Override
        public void call(BonjourService bonjourService) {
        	Log.d("TAG", bonjourService.toString());
        }
    }, new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
        	Log.e("TAG", "error", throwable);
        }
	});
```

### Rx2DNSSD

- Rx2DnssdBindable
```java
Rx2Dnssd rxdnssd = new Rx2DnssdBindable(context);
```

- Rx2DnssdEmbedded
```java
Rx2Dnssd rxdnssd = new Rx2DnssdEmbedded();
```

##### Register service

```java
BonjourService bs = new BonjourService.Builder(0, 0, Build.DEVICE, "_rxdnssd._tcp", null).port(123).build();
registerDisposable = rxDnssd.register(bs)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(bonjourService -> {
            Log.i("TAG", "Register successfully " + bonjourService.toString());
        }, throwable -> {
            Log.e("TAG", "error", throwable);
        });
```

##### Browse services example

```java
browseDisposable = rxDnssd.browse("_http._tcp", "local.")
        .compose(rxDnssd.resolve())
        .compose(rxDnssd.queryRecords())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(bonjourService -> {
            Log.d("TAG", bonjourService.toString());
            if (bonjourService.isLost()) {
                mServiceAdapter.remove(bonjourService);
            } else {
                mServiceAdapter.add(bonjourService);
            }
        }, throwable -> Log.e("TAG", "error", throwable));
```

License
-------
	Copyright (C) 2021 Andriy Druk
	Copyright (C) 2022 DittoLive Incorporated

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
