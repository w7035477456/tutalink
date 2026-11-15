module.exports = {
  apps: [{
    name: "tutalinkPM2Process",
    script: "java",
    args: "-Xms256m -Xmx512m -jar target/tutalink.jar --spring.profiles.active=prod",
    cwd: "/home/lawsen0/code/tutalink",
    exec_mode: "fork",
    instances: 1,
    autorestart: true,
    max_restarts: 10,
    kill_timeout: 30000
  }]
};
