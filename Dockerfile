# Android APK build image for Napominalka (Windows-friendly)
FROM ghcr.io/cirruslabs/android-sdk:34

# Install utilities and Gradle
USER root
RUN apt-get update && apt-get install -y wget unzip && rm -rf /var/lib/apt/lists/*

# Gradle installation (adjust version if needed)
ENV GRADLE_VERSION=8.5
RUN wget -q https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip \
    && unzip gradle-${GRADLE_VERSION}-bin.zip -d /opt \
    && rm gradle-${GRADLE_VERSION}-bin.zip
ENV GRADLE_HOME=/opt/gradle-${GRADLE_VERSION}
ENV PATH="$GRADLE_HOME/bin:$PATH"

# Android SDK env
ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

# Accept licenses and install platforms/build-tools (cover common versions)
RUN yes | sdkmanager --licenses && sdkmanager \
    "platform-tools" \
    "cmdline-tools;latest" \
    "platforms;android-33" \
    "platforms;android-34" \
    "build-tools;33.0.2" \
    "build-tools;34.0.0"

WORKDIR /workspace