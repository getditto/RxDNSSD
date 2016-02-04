/*
 * Copyright (C) 2016 Andriy Druk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.druk.rxdnssd;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A class representing bonjour service
 */
public class BonjourService implements Parcelable {

    /**
     * Flag that indicate that Bonjour service was lost
     */
    public static final int LOST = ( 1 << 8 );

    private final int flags;
    private final String serviceName;
    private final String regType;
    private final String domain;
    private final Inet4Address inet4Address;
    private final Inet6Address inet6Address;
    private final Map<String, String> dnsRecords;
    private final int ifIndex;
    private final String hostname;
    private final int port;

    protected BonjourService(Builder builder) {
        this.flags = builder.flags;
        this.serviceName = builder.serviceName;
        this.regType = builder.regType;
        this.domain = builder.domain;
        this.ifIndex = builder.ifIndex;
        this.inet4Address = builder.inet4Address;
        this.inet6Address = builder.inet6Address;
        this.dnsRecords = Collections.unmodifiableMap(builder.dnsRecords);
        this.hostname = builder.hostname;
        this.port = builder.port;
    }

    /** Get flags */
    public int getFlags() {
        return flags;
    }

    /** Get the service name */
    public String getServiceName() {
        return serviceName;
    }

    /** Get reg type */
    public String getRegType() {
        return regType;
    }

    /** Get domain */
    public String getDomain() {
        return domain;
    }

    /** Get if index */
    public int getIfIndex() {
        return ifIndex;
    }

    /** Get hostname */
    public String getHostname() {
        return hostname;
    }

    /** Get port */
    public int getPort() {
        return port;
    }

    /** Get TXT records */
    public Map<String, String> getTxtRecords() {
        return dnsRecords;
    }

    /** Get ipv4 address */
    public Inet4Address getInet4Address() {
        return inet4Address;
    }

    /** Get ipv6 address */
    public Inet6Address getInet6Address() {
        return inet6Address;
    }

    /** Get status of bonjour service
     *
     * @return true if service was lost
     */
    public boolean isLost() {
        return (flags & LOST) != LOST;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BonjourService that = (BonjourService) o;
        return serviceName.equals(that.serviceName) && regType.equals(that.regType) && domain.equals(that.domain);
    }

    @Override
    public int hashCode() {
        int result = serviceName.hashCode();
        result = 31 * result + regType.hashCode();
        result = 31 * result + domain.hashCode();
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.flags);
        dest.writeString(this.serviceName);
        dest.writeString(this.regType);
        dest.writeString(this.domain);
        writeMap(dest, this.dnsRecords);
        dest.writeSerializable(this.inet4Address);
        dest.writeSerializable(this.inet6Address);
        dest.writeInt(this.ifIndex);
        dest.writeString(this.hostname);
        dest.writeInt(this.port);
    }

    protected BonjourService(Parcel in) {
        this.flags = in.readInt();
        this.serviceName = in.readString();
        this.regType = in.readString();
        this.domain = in.readString();
        this.dnsRecords = readMap(in);
        this.inet4Address = (Inet4Address) in.readSerializable();
        this.inet6Address = (Inet6Address) in.readSerializable();
        this.ifIndex = in.readInt();
        this.hostname = in.readString();
        this.port = in.readInt();
    }

    public static final Parcelable.Creator<BonjourService> CREATOR = new Parcelable.Creator<BonjourService>() {
        public BonjourService createFromParcel(Parcel source) {
            return new BonjourService(source);
        }

        public BonjourService[] newArray(int size) {
            return new BonjourService[size];
        }
    };

    private static void writeMap(Parcel dest, Map<String, String> val) {
        if (val == null) {
            dest.writeInt(-1);
            return;
        }
        int N = val.size();
        dest.writeInt(N);
        for (String key : val.keySet()) {
            dest.writeString(key);
            dest.writeString(val.get(key));
        }
    }

    private static Map<String, String> readMap(Parcel in) {
        int N = in.readInt();
        if (N < 0) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < N; i++) {
            result.put(in.readString(), in.readString());
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public String toString() {
        return "BonjourService{" +
                "serviceName='" + serviceName + '\'' +
                ", regType='" + regType + '\'' +
                ", domain='" + domain + '\'' +
                '}';
    }

    public static class Builder {
        private final int flags;
        private final String serviceName;
        private final String regType;
        private final String domain;
        private final int ifIndex;
        private Inet4Address inet4Address;
        private Inet6Address inet6Address;
        //mutable version
        private Map<String, String> dnsRecords = new HashMap<>();
        private String hostname;
        private int port;

        /**
         * Constructs a builder initialized to input parameters
         *
         * @param flags         flags of BonjourService.
         * @param ifIndex       ifIndex of BonjourService.
         * @param serviceName   serviceName of BonjourService.
         * @param regType       regType of BonjourService.
         * @param domain        domain of BonjourService.
         *
         */
        public Builder(int flags, int ifIndex, String serviceName, String regType, String domain) {
            this.flags = flags;
            this.serviceName = serviceName;
            this.regType = regType;
            this.domain = domain;
            this.ifIndex = ifIndex;
        }

        /**
         * Constructs a builder initialized to the contents of existed BonjourService object
         *
         * @param service the initial contents of the object.
         */
        public Builder(BonjourService service) {
            this.flags = service.flags;
            this.serviceName = service.serviceName;
            this.regType = service.regType;
            this.domain = service.domain;
            this.ifIndex = service.ifIndex;
            this.dnsRecords = new HashMap<>(service.dnsRecords);
            this.inet4Address = service.inet4Address;
            this.inet6Address = service.inet6Address;
            this.hostname = service.hostname;
            this.port = service.port;
        }

        /**
         * Appends hostname of service
         *
         * @param hostname the hostname of service.
         * @return this builder.
         */
        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        /**
         * Appends port
         *
         * @param port the port of service.
         * @return this builder.
         */
        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Appends TXT records of service
         *
         * @param dnsRecords map of TXT records.
         * @return this builder.
         */
        public Builder dnsRecords(Map<String, String> dnsRecords) {
            this.dnsRecords = dnsRecords;
            return this;
        }

        /**
         * Appends ipv4 address
         *
         * @param inet4Address ipv4 address of service.
         * @return this builder.
         */
        public Builder inet4Address(Inet4Address inet4Address) {
            this.inet4Address = inet4Address;
            return this;
        }

        /**
         * Appends ipv6 address
         *
         * @param inet6Address ipv6 address of service.
         * @return this builder.
         */
        public Builder inet6Address(Inet6Address inet6Address) {
            this.inet6Address = inet6Address;
            return this;
        }

        /**
         * Constructs a BonjourService object
         *
         * @return new BonjourService object.
         */
        public BonjourService build() {
            return new BonjourService(this);
        }

    }
}