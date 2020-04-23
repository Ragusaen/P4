typedef DigitalOutputPin int;
typedef Time int;

DigitalOutputPin led = 13;


struct thismodule_t {
    Int a;
} thismodule;

void thismodule() {
    digitalWrite(led, HIGH);

    schedule(currentTime + 50)

    digitalWrite(led, LOW);
    thismodule.a += 1;
}

void setup() {
    pinMode(led, OUTPUT);
}


void loop() {

}