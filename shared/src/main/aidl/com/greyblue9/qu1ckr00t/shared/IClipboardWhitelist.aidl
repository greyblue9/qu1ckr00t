package com.greyblue9.qu1ckr00t.shared;

interface IClipboardWhitelist {
    int version();

    String[] queryPackages();
    void addPackage(String packageName);
    void removePackage(String packageName);
}