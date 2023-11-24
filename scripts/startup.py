import json
import os
import requests

base_url = 'http://localhost:8000/api/'
predicates = {}

# Startup script for adding data that is needed by the frontend
# TODO: To improve the speed, use Cypher queries here instead of the API

def readFile(path):
    """
    Reads the json file and returns a dictionary containing the json object
    
    Parameters
    ----------
    path : str
        The local path of the json file
    """
    
    file = open(path)
    json_string = json.load(file)
    file.close()
    return json_string

def createResource(label):
    """
    Create a new resoruce using the provided label
    
    Parameters
    ----------
    label : str
        The label of the resource
    """
    
    data = {"label" : label}
    return createPost(base_url+'resources/',data).json()

def createLiteral(label):
    """
    Create a new literal using the provided label
    
    Parameters
    ----------
    label : str
        The label of the literal
    """
    data = {"label" : label}
    return createPost(base_url+'literals/',data).json()

def createPredicate(label):
    """
    Create a new predicate using the provided label
    
    Parameters
    ----------
    label : str
        The label of the predicate
    """
    data = {"label" : label}
    return createPost(base_url+'predicates/',data).json()

def createStatement(sub, pred, obj):
    """
    Create a literal statment using the provided subject, predicate, and object
    
    Parameters
    ----------
    sub : int
        The id of the subject resource
    pred : int
        The id of the predicate
    obj : int
        The id of the literal resource to be placed in the object position
    """
    data = {"subject_id" : sub, "predicate_id" : pred, "object": { "id": obj, "_class": "literal" } }
    resp = requests.post('{}statements/'.format(base_url), json=data)
    if resp.status_code != 201:
        print(resp.text)
    return resp

def createResourceStatement(sub, pred, obj):
    """
    Create a literal statment using the provided subject, predicate, and object
    
    Parameters
    ----------
    sub : int
        The id of the subject resource
    pred : int
        The id of the predicate
    obj : int
        The id of the object resource (can't be a literal)
    """
    data = {"subject_id" : sub, "predicate_id" : pred, "object": { "id": obj, "_class": "resource" } }
    resp = requests.post('{}statements/'.format(base_url), json=data)
    if resp.status_code != 201:
        print(resp.text)
    return resp

def createPost(url, data):
    """
    Internal call for the API
    
    Parameters
    ----------
    url : str
        the url of the api endpoint
    data : dict
        the json data as a dictionary object
    """
    resp = requests.post(url, json = data)
    if resp.status_code != 201:
        print(resp.text)
    return resp

def createOrFindPredicate(label):
    """
    Create a new predicate but not before looking it up in the in-memory list of created predicates
    (should be imporved to llok up in the neo4j to avoid duplicate predicates)
    
    Parameters
    ----------
    label : str
        The label of the predicate
    """
    if label in predicates:
        return predicates[label]
    else:
        pred = createPredicate(label)
        predicates[pred['label']]=pred['id']
        return pred['id']
     
if __name__ == '__main__':
    # default system-wide predicates
    createOrFindPredicate("has doi")
    createOrFindPredicate("has author")
    createOrFindPredicate("has publication month")
    createOrFindPredicate("has publication year")
    createOrFindPredicate("has research field")
    createOrFindPredicate("has contribution")
    createOrFindPredicate("has research problem")
    createOrFindPredicate("is a")

    # demo predicate data 
    createOrFindPredicate("approach")
    createOrFindPredicate("evaluation")
    createOrFindPredicate("implementation")

    createResource("paper")

    research_field = createResource("Research field")['id']

    pred_id = createOrFindPredicate("has subfield")

    researchFields = readFile(os.path.dirname(__file__) + '/ResearchFields.json')

    print("Adding research fields...")
    for field in researchFields:
        objectNew = createResource(field['name'])['id']

        createResourceStatement(research_field, pred_id, objectNew)

        if "subfields" in field:
            for subfield in field['subfields']:
                objectNew2 = createResource(subfield['name'])['id']

                createResourceStatement(objectNew, pred_id, objectNew2)

                if "subfields" in subfield:
                    for subfield2 in subfield['subfields']:
                        objectNew3 = createResource(subfield2['name'])['id']

                        createResourceStatement(objectNew2, pred_id, objectNew3)
    
    print("Done")
