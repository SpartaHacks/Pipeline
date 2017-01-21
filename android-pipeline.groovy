def jenkinsBuild() {
  node ('android_build') {
    echo "Hello, world!"
  }
}

return this;
