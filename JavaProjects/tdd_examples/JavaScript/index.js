function sum(a, b) {
    return a + b;
}

function calculator_add(input){
if(input == undefined || input == "")
    return 0;
var inputArr = input.split(',');
if(inputArr.length == 1)
    return parseInt(inputArr[0]);
return parseInt(inputArr[0])+parseInt(inputArr[1]);
}
module.exports = {sum:sum,calculator_add:calculator_add};