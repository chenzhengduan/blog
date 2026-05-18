import random

random_num=random.randint(1,10)

isFlag = False

while True:

  phone = input("请输入一个数字:")

  if(phone == str(random_num)):
    print("回答正确")
    break
  elif int(phone) > random_num:
    print("你输入的数字太大了")
  else :
    print("你输入的数字太小了")


