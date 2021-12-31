package com.greyblue9.qu1ckr00t.shared;

interface IQu1ckroot {
    int version();

    String[] queryPackages();
    void addPackage(String packageName);
    void removePackage(String packageName);
}