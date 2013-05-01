import shelve

def addRoom(room,password):
    rooms = shelve.open('rooms')
    data = {"name":room, "pass":password}
    rooms[str(room)] = data
    rooms.close()
    return True

def roomExists(room):
    rooms = shelve.open('rooms')
    if str(room) in rooms:
        return True
    else:
        return False

#assumes room exists so do that check first
def validatePassword(room,password):
    rooms = shelve.open('rooms')
    return rooms[str(room)]['pass'] == str(password)

def delRoom(room):
    rooms = shelve.open('rooms')
    del rooms[room]
    
#tests
#addRoom('blue','password1')
#print roomExists('blue')
#print validatePassword('blue','password1')

