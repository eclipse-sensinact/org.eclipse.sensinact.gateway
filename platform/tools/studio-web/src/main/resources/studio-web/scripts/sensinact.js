function light(sw) {
  var pic;
  if (sw == 0) {
    pic = "./images/off.png"
  } else {
    pic = "./images/on.png"
  }
  document.getElementById('sensinact').src = pic;
  }
