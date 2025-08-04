package main

import (
	"fmt"
)

type Animo interface {
	run(startTime string, endTime string)
	sleep(endTime string)
}

type Dog struct {
	name     string
	age      int
	birthDay string
	runCount int
}
type Cat struct {
	name       string
	sleepCount int
}

func (d *Dog) run(startTime string, endTime string) {
	d.runCount += 1
	fmt.Printf("dog name is %s, now age is %d, dog is running", d.name, d.age)
}
func (d *Dog) sleep(endTime string) {
	fmt.Printf("dog name is %s, now age is %d, dog is sleep at %s", d.name, d.age, endTime)
}

func (c *Cat) run(startTime string, endTime string) {
	fmt.Printf("cat name is %s, cat is running", c.name)
}
func (c *Cat) sleep(endTime string) {
	c.sleepCount += 1
	fmt.Printf("cat name is %s, cat is sleep at %s", c.name, endTime)
}

// func main() {
// 	var dog Animo = &Dog{name: "abc", age: 2}
// 	dog.run("10:00", "11:00")
// 	fmt.Printf("dog fun count is %d"+"\n", dog.(*Dog).runCount)

// 	var cat Animo = &Cat{}
// 	cat.(*Cat).name = "tom"
// 	cat.sleep("12:00")
// 	fmt.Printf("dog fun count is %d"+"\n", cat.(*Cat).sleepCount)

// }
