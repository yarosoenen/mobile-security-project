Java.perform(function () {
  var Activity = Java.use("com.example.ms_project.MainActivity");

  Activity.checkForPasswordMatch.implementation = function () {
    return true;
  };
});
