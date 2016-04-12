#!/usr/bin/env python

import pickle

train_file = "FB15k/freebase_mtr100_mte100-train.txt"
test_file = "FB15k/freebase_mtr100_mte100-test.txt"
valid_file = "FB15k/freebase_mtr100_mte100-valid.txt"

out_file = "fb.bin"

class Dictionary(object):
    def __init__(self):
        self.strings = dict()
        # Confusing, maybe, but I would like this to be 1-indexed, not
        # 0-indexed
        self.array = [-1]
        self.current_index = 1

    def getIndex(self, string, _force_add=False):
        if not _force_add:
            index = self.strings.get(string, None)
            if index:
                return index
        self.strings[string] = self.current_index
        self.array.append(string)
        self.current_index += 1
        return self.current_index - 1

    def getString(self, index):
        return self.array[index]

    def getAllStrings(self):
        return self.array[1:]

def getTriplesFromFile(filename, entityDict, relDict):
    triples = []
    for line in open(filename):
        fields = line.split()
        source = fields[0]
        relation = fields[1]
        target = fields[2]
        sourceIndex = entityDict.getIndex(source)
        targetIndex = entityDict.getIndex(target)
        relationIndex = relDict.getIndex(relation)
        triple = (sourceIndex, targetIndex, relationIndex)
        triples.append(triple)
    return triples


def main():
    entityDict = Dictionary()
    relDict = Dictionary()
    trainTriples = getTriplesFromFile(train_file, entityDict, relDict)
    testTriples = getTriplesFromFile(test_file, entityDict, relDict)
    validTriples = getTriplesFromFile(valid_file, entityDict, relDict)
    data = {}
    data['train_subs'] = trainTriples
    data['test_subs'] = testTriples
    data['valid_subs'] = validTriples
    data['entities'] = entityDict.getAllStrings()
    data['relations'] = relDict.getAllStrings()
    with open(out_file, 'wb') as out:
        pickle.dump(data, out, protocol=2)




if __name__ == '__main__':
    main()

# vim: et sw=4 sts=4
