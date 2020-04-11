package main

import (
	"fmt"
	"os"
)

func main() {
	file, err := os.Create(`./args.txt`)
	if err != nil {
		return
	}
	defer file.Close()

	for _, v := range os.Args {
		_, err := fmt.Fprintf(file, "%s\n", v)
		if err != nil {
			return
		}
	}
}
