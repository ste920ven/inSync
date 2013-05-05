import shelve

def addRoom(room,password):
    rooms = shelve.open('rooms')
    data = {"name":room, "pass":password, 'users':[]}
    rooms[str(room)] = data
    rooms.close()
    return True

def roomExists(room):
    rooms = shelve.open('rooms')
    if str(room) in rooms:
        return True
    else:
        return False

def addUserToRoom(room,nickname,nid):
    rooms = shelve.open('rooms')
    da = [nickname, nid]
    r = rooms[str(room)]['users']
    r.append(da)
    data = {"name":room, "pass":rooms[str(room)]['pass'], "users":r }
    rooms[str(room)] = data

def getUsersFromRoom(room):
    rooms = shelve.open('rooms')
    return rooms[str(room)]['users']

#assumes room exists so do that check first
def validatePassword(room,password):
    rooms = shelve.open('rooms')
    return rooms[str(room)]['pass'] == str(password)

def delRoom(room):
    rooms = shelve.open('rooms')
    del rooms[room]
    
#tests
#print roomExists('blue')
#print validatePassword('blue','password1')
