val suits = listOf('♦', '♥', '♠', '♣')
val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
var endGame = false
val deck = mutableListOf<String>()
val player = Player(name = "Player", id = 0)
val computer = Player(name = "Computer", id = 1)
val tableCards = mutableListOf<String>()
var lastWon: Int? = null //null: nobody, 0: player, 1:PC
var firstToPlay = computer // Computer plays first
var secondToPlay = player

fun main() {
    println("Indigo card Game")
    while (true) {
        println("Play first?")
        val input = readln().trim().uppercase()
        if (input == "YES" || input == "NO") {
            if (input == "YES") {
                firstToPlay = player    // Player plays first
                secondToPlay = computer
            }
            break
        }
    }
    initialize()
    loop@ while (true) {
        while (!(deck.isEmpty() && player.handCards.isEmpty() && computer.handCards.isEmpty())) {
            //if players don't have cards give them 6
            if (player.handCards.isEmpty() && computer.handCards.isEmpty()) getPlayerCards()
            if (!playerMove(firstToPlay)) break@loop
            if (!playerMove(secondToPlay)) break@loop
        }
        cardsOnTable()
        giveRemainingCards()
        calcScore(endGame = true)
        break@loop
    }
    print("Game Over")
}

fun giveRemainingCards() {
    var p: Player = firstToPlay
    when (lastWon) {
        null -> p = firstToPlay
        0 -> p = player
        1 -> p = computer
        else -> Unit
    }
    p.wonCards += tableCards
    tableCards.clear()
}

fun cardsOnTable() {
    if (tableCards.isNotEmpty()) {
        println("${tableCards.size} cards on the table, and the top card is ${tableCards.last()}")
    } else println("No cards on the table")
}

fun playerMove(p: Player): Boolean {
    cardsOnTable()
    if (p.id == 0) { //p is Player
        print("Cards in hand: ")
        p.handCards.forEach { print("${player.handCards.indexOf(it) + 1})$it ") }
        println()
        while (true) {
            println("Choose a card to play (1-${p.handCards.size}):")
            val input = readln()
            if (input == "exit") return false
            val cardToPlay = input.toIntOrNull() ?: 0
            if (cardToPlay in 1..p.handCards.size) {
                playCard(p, p.handCards[cardToPlay - 1])
                break
            }
        }
    } else {  //p is Computer
        p.handCards.forEach { print("$it ") }
        println()
        val cardPlayed = whichCardPcPlays(p.handCards)
        println("Computer plays $cardPlayed")
        playCard(p, cardPlayed)
    }
    return true
}

fun whichCardPcPlays(cards: List<String>): String {
    val table = if (tableCards.isNotEmpty()) tableCards.last() else null
    val suits = cards.map { it.last() }
    val ranks = cards.map { it.dropLast(1) }
    val candidateCards = cards.filter { it.last() == table?.last() || it.dropLast(1) == table?.dropLast(1) }
    //println("Candidate cards: $candidateCards")
    // only 1 card in hand
    if(cards.size == 1) return cards[0]
    //if only 1 candidate card
    else if(candidateCards.size == 1) return candidateCards[0]
    //No cards on the table or no candidates
    else if(table == null || candidateCards.isEmpty()) {
        //find multiple suits and multiple ranks in hand
        val multiSuits = suits.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        val multiRanks = ranks.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        //println("Multiple suits: $multiSuits")
        //println("Multiple ranks: $multiRanks")
        //if multi suits play a random card of them
        if (multiSuits.isNotEmpty()) {
            val multiSuitCards = cards.filter { multiSuits.contains(it.last()) }
            return multiSuitCards.random()
        }
        //if multi ranks play a random card of them
        else if (multiRanks.isNotEmpty()) {
            val multiRankcards = cards.filter { multiRanks.contains(it.dropLast(1)) }
            return multiRankcards.random()
        } else return cards.random()
    }
    //if more than 1 candidate cards
    else if(candidateCards.size > 1) {
        val fitSuit = candidateCards.filter{ it.last() == table?.last() }
        val fitRank = candidateCards.filter{ it.dropLast(1) == table?.dropLast(1) }
        if (fitSuit.size >= 2) {
            return fitSuit.random()
        }
        else if (fitRank.size >= 2){
            return fitRank.random()
        }
        else return candidateCards.random()
    }
    return cards[0]
}

fun playCard(p: Player, card: String) {
    var win = false
    if (tableCards.isNotEmpty()) {
        val topCard = tableCards.last()
        win = card.last() == topCard.last() || card.dropLast(1) == topCard.dropLast(1)
    }
    p.handCards.remove(card)
    tableCards += card

    if (win) {
        println("${p.name} wins cards")
        lastWon = p.id
        p.wonCards += tableCards
        tableCards.clear()
        calcScore()

    }
}

fun calcScore(endGame: Boolean = false) {
    val winningRanks = listOf("A", "K", "Q", "J", "10")
    player.score = 0
    computer.score = 0
    player.wonCards.forEach { if (winningRanks.contains(it.dropLast(1))) player.score++ }
    computer.wonCards.forEach { if (winningRanks.contains(it.dropLast(1))) computer.score++ }
    if (endGame) {
        if (player.wonCards.size == computer.wonCards.size) firstToPlay.score += 3
        else if (player.wonCards.size > computer.wonCards.size) player.score += 3
        else computer.score += 3
    }
    println("Score: Player ${player.score} - Computer ${computer.score}")
    println("Cards: Player ${player.wonCards.size} - Computer ${computer.wonCards.size}")
}

fun reset() {
    deck.clear()
    for (suit in suits) {
        ranks.forEach { deck.add("$it$suit") }
    }
}

fun shuffle() {
    deck.shuffle()
}

fun getCards(number: Int): List<String> {
    if (number !in 1..52) println("Invalid number of cards.")
    else if ((number ?: 0) > deck.size) {
        println("The remaining cards are insufficient to meet the request.")
    } else {
        val cards = deck.take(number)
        deck -= cards
        return cards
    }
    return listOf()
}

fun initialize() {
    reset()
    shuffle()
    print("Initial cards on the table:")
    tableCards.also { it.clear() }.addAll(getCards(4))
    tableCards.forEach { print("$it ") }
    println()
}

fun getPlayerCards() {
    player.handCards.also { it.clear() }.addAll(getCards(6))
    computer.handCards.also { it.clear() }.addAll(getCards(6))
}

data class Player(
    val name: String = "",
    val id: Int? = null,
    val handCards: MutableList<String> = mutableListOf<String>(),
    val wonCards: MutableList<String> = mutableListOf<String>(),
    var score: Int = 0,
)