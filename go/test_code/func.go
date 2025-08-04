package main

import (
	"errors"
	"fmt"
	"os"
	"strconv"
	"time"
)

func init() {
	fmt.Println("init")
}

func Plus(a int, b int) (int, error) {
	if a < 0 {
		return 0, errors.New("a is less than 0")
	}
	return a + b, nil
}

func main1() {
	var first = os.Args[1]
	var second = os.Args[2]

	fmt.Println("first=", first)
	fmt.Println("second=", second)

	var firstInt, _ = strconv.Atoi(first)
	var secondInt, _ = strconv.Atoi(second)

	if num, err := Plus(firstInt, secondInt); err == nil {
		fmt.Println("num=", num)
	} else {
		fmt.Println("错误:", err)
	}

	var a int = 1

	a = 2
	fmt.Println("a=", a)

	ch := make(chan int)

	go func() {
		ch <- 1
	}()

	fmt.Println("ch=", <-ch)

	go func() {
		for res := range ch {
			fmt.Printf("ch->%d\n", res)
		}
	}()

	ch <- 10
	ch <- 20

	close(ch)
	time.Sleep(time.Microsecond * 100)

	return

}
