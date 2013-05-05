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

def addUserToRoom(room,nickname,nid,state,typ):
    rooms = shelve.open('rooms')
    da = [nickname, nid, state, typ]
    r = rooms[str(room)]['users']
    for user in r:
        if user == da:
            return False
    r.append(da)
    data = {"name":room, "pass":rooms[str(room)]['pass'], "users":r }
    rooms[str(room)] = data

def getUsersFromRoom(room):
    rooms = shelve.open('rooms')
    return rooms[str(room)]['users']

def updateUser(room,name,nid,ntype):
    rooms = shelve.open('rooms')
    du = [name, nid, False, ntype]
    r = rooms[str(room)]['users']
    boo = False
    r = [user for user in r if user != du]
    nda = [name,nid,True,ntype]
    for user in r:
        if user == nda:
            boo = True
            break
    if boo == False:
        r.append(nda)
    data = {"name":room, "pass":rooms[str(room)]['pass'], "users":r}
    rooms[str(room)] = data
    
def clearUsers(room):
    rooms = shelve.open('rooms')
    data = {"name":room, "pass":rooms[str(room)]['pass'], "users":[]}
    rooms[str(room)] = data
    
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
#addUserToRoom('test','b','1',False)
#print getUsersFromRoom('test')
#addUserToRoom('test','b','1',False)
#print getUsersFromRoom('test')

