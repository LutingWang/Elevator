import random


def generate_person():
    from_floor = random.randint(-3, 20)
    while from_floor == 0:
        from_floor = random.randint(-3, 20)
    to_floor = random.randint(-3, 20)
    while to_floor == from_floor or to_floor == 0:
        to_floor = random.randint(-3, 20)
    return from_floor, to_floor


if __name__ == '__main__':
    id = 0
    with open("./datacheck_in.txt", 'w') as f:
        for i in range(40):
            id += 1
            from_floor, to_floor = generate_person()
            f.write("[0.0]{}-FROM-{}-TO-{}\n".format(id, from_floor, to_floor))
