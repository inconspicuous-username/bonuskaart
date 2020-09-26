package main

import (
	"bufio"
	"encoding/json"
	"fmt"
	"log"
	"math/rand"
	"net/http"
	"os"
	"strconv"
	"time"
)

var bonusCards []string

func getBonusKaart(w http.ResponseWriter, r *http.Request) {
	if len(bonusCards) <= 0 {
		return
	}
	message := bonusCards[rand.Intn(len(bonusCards))]
	// Tell the client not to cache the result.
	// This function is only called when the client explicitly
	// indicates that they want a new bonuskaart.
	w.Header().Set("Cache-Control", "no-cache")
	w.WriteHeader(http.StatusOK)
	w.Write([]byte(message))

}

func getMultipleCards(w http.ResponseWriter, r *http.Request) {
	// message := "OK"

	numberS := r.FormValue("number")
	number, err := strconv.Atoi(numberS)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte("could not be converted to number."))
		return
	}
	if (number > len(bonusCards)) || (number > 5) || (number < 1) {
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte("Request must between 1-5 cards"))
		return
	}

	selectedCards := selectRandomSubset(number, bonusCards)
	byte_cards, err := json.Marshal(selectedCards)

	w.Header().Set("Cache-Control", "no-cache")
	w.WriteHeader(http.StatusOK)
	w.Write([]byte(byte_cards))
}

func getNumbberOfCards(w http.ResponseWriter, r *http.Request) {
	Response := fmt.Sprintf("%d", len(bonusCards))
	// Tell the client not to cache the result.
	w.Header().Set("Cache-Control", "no-cache")
	w.WriteHeader(http.StatusOK)
	w.Write([]byte(Response))
}

func giveBonusKaart(w http.ResponseWriter, r *http.Request) {
	if r.Method != "POST" {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}
	// A quick check to weed out most of the non-sensical applications
	kaart := r.FormValue("kaart")
	if len(kaart) != 13 {
		w.Write([]byte(":/"))
		return
	}
	_, err := strconv.Atoi(kaart)
	if err != nil {
		w.Write([]byte(":/"))
		return
	}

	// For now we abuse the logs to store the newly donated bonuskaart
	log.Println("New bonuscard donated!!: ", r.FormValue("kaart"))
	message := "<h1 style=\"font-size:40px;\"> Thanks! </1>"
	w.Write([]byte(message))
}

func serveFiles(w http.ResponseWriter, r *http.Request) {
	p := "./static" + r.URL.Path
	http.ServeFile(w, r, p)
}

func main() {
	log.Println("Application started")
	defer log.Println("Application stopped")
	rand.Seed(time.Now().UnixNano())
	var err error
	err, bonusCards = readFile()
	if err != nil {
		log.Fatal(err)
	}

	http.HandleFunc("/", serveFiles)
	http.HandleFunc("/GetCard", getBonusKaart)
	http.HandleFunc("/GiveCard", giveBonusKaart)
	http.HandleFunc("/getMultipleCards", getMultipleCards)
	http.HandleFunc("/GetNumberOfCards", getNumbberOfCards)
	if err := http.ListenAndServe(":8123", nil); err != nil {
		log.Fatal(err)
	}
}

func readFile() (error, []string) {
	var cards []string
	file, err := os.Open("bonuskaart.txt")
	if err != nil {
		return err, nil
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := scanner.Text()
		if len(line) != 13 {
			log.Println("Bonuskaart barcodes need to be 13 characters long, got ", len(line))
		} else {
			cards = append(cards, line)
		}
	}

	if err := scanner.Err(); err != nil {
		return err, nil
	}
	return nil, cards
}

func stringInSlice(a string, list []string) bool {
	for _, b := range list {
		if b == a {
			return true
		}
	}
	return false
}

func selectRandomSubset(length int, list []string) []string {
	selectedcards := make([]string, length)
	var canditate string
	for i, _ := range selectedcards {
		canditate = list[rand.Intn(len(list))]
		// Check if randomyl selected card isn't already selected.
		for stringInSlice(canditate, selectedcards) {
			canditate = list[rand.Intn(len(list))]
		}
		selectedcards[i] = canditate
	}
	return selectedcards
}
