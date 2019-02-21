import json
import requests

base_url='http://localhost:8000/api/'
predicates = {}
implementations = {}
resources = {}
notfound = []
tasks = {}

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
    label : int
        The label of the predicate
    """
    if label in predicates:
        return predicates[label]
    else:
        pred = createPredicate(label)
        predicates[pred['label']]=pred['id']
        return pred['id']

def createPaperSubgraph(obj):
    """
    Create the subgraph relating to the provided json strucutre,
    this subgraph corresponds to one of the paperswithcode data files
     ('papers-with-abstracts.json')
    
    Parameters
    ----------
    obj : dict
        the json representation of the paper
    """
    arxiv_id = obj["arxiv_id"]
    title = obj["title"]
    abstract = obj["abstract"]
    url = obj["url_pdf"]
    proceeding = obj["proceeding"]
    #--------------------------------------
    RC = createResource(title)['id']
    resources[title] = RC
    #--------------------------------------
    if arxiv_id is not None:
        l_arxiv = createLiteral(arxiv_id)['id']
        p_arxiv = createOrFindPredicate('has arxiv id')
        createStatement(RC,p_arxiv,l_arxiv)
    #--------------------------------------
    if title is not None:
        l_title = createLiteral(title)['id']
        p_title = createOrFindPredicate('has title')
        createStatement(RC,p_title,l_title)
    #--------------------------------------
    if abstract is not None:
        l_abstract = createLiteral(abstract)['id']
        p_abstract = createOrFindPredicate('has abstract')
        createStatement(RC,p_abstract,l_abstract)
    #--------------------------------------
    if url is not None:
        l_url = createLiteral(url)['id']
        p_url = createOrFindPredicate('has url')
        createStatement(RC,p_url,l_url)
    #--------------------------------------
    if proceeding is not None:
        l_proceeding = createLiteral(proceeding)['id']
        p_proceeding = createOrFindPredicate('has proceeding')
        createStatement(RC,p_proceeding,l_proceeding)
    #print("added paper ({})".format(title))
    
def createCodeSubgraph(obj):
    """
    Create the subgraph relating to the provided json strucutre,
    this subgraph corresponds to one of the paperswithcode data files
    ('links-between-papers-and-code.json'), it creates an implementation node
    and appends the repo url to that node
    
    Note: it needs to be imporved to lookup in the Neo4J if the implementation node already exists
    
    Parameters
    ----------
    obj : dict
        the json representation of the paper
    """
    title = obj["paper_title"]
    repo_url = obj["repo_url"]
    if title not in resources:
        print("resource not found")
        notfound.append(title)
        return
    if title in implementations:
        impl_id = implementations[title]
    else:
        impl_id = createResource("(Implementation) {}".format(title))['id']
        implementations[title] = impl_id
    p_repo_url = createOrFindPredicate('has repo url')
    l_repo_url = createLiteral(repo_url)['id']
    createStatement(impl_id,p_repo_url,l_repo_url)
    impl_pred = createOrFindPredicate('has implementation')
    createResourceStatement(resources[title],impl_pred,impl_id)
    #print("Implementation added for ({})".format(title))
    
def findPaperInMemory(title, collection, key):
    """
    Find a paper json object in the in-memory json representation lists
    
    Returns the json dict object if found, and NoneType otherwise
    
    Parameters
    ----------
    title : string
        the title of the paper in question
    collection : list
        the json list object to be searched in
    key : str
        the key to use to look up the title and compare it
    """
    for paper in collection:
        if paper[key] == title:
            return paper
    return None
    

if __name__ == '__main__':
    papersWithAbstracts = readFile('papers-with-abstracts.json')
    papersWithCode = readFile('links-between-papers-and-code.json')
    evalTables = readFile('evaluation-tables.json')
    #-------------------------------------------------
    for index, paper in enumerate(papersWithAbstracts):
        if paper['title'] is None:
            continue
        #if index > 10000:
        #    break
        createPaperSubgraph(paper)
        if index % 50 == 0:
            print("Paper #{} done".format(index))
    #-------------------------------------------------
    for index, paper in enumerate(papersWithCode):
        createCodeSubgraph(paper)
        if index % 50 == 0:
            print("Implementation #{} done".format(index))
    for node in notfound:
        new = createResource(node)['id']
        resources[node] = new
        result = findPaperInMemory(node, papersWithCode, 'paper_title')
        createCodeSubgraph(result)
   #-------------------------------------------------
   