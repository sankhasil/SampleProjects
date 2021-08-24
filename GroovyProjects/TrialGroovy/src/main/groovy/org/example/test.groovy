package org.example

class TestClass {

    static void main(args) {
        print 'Testing Groovy'

        // readFileInList('/home/sankha/SampleProjects/GroovyProjects/TrialGroovy/src/main/resources/Jenkinsfile').each { line ->
        //     if (line.contains('ANTORA_IMAGE = "docker.kiwigrid.com/kos/antora-customized:')) {
        //         print 'found entry'
        //     }
        // }
        def jenkinsfileContent = readFileInList('/home/sankha/SampleProjects/GroovyProjects/TrialGroovy/src/main/resources/Jenkinsfile')
        def editedContent = jenkinsfileContent.collect { item ->
            item.contains('ANTORA_IMAGE = "docker.kiwigrid.com/kos/antora-customized:') ? '                ANTORA_IMAGE = "docker.kiwigrid.com/kos/antora-customized:1.0.0"' : item
        }
        //editedContent.each { line ->
          //  println line
       //}
      def joinedString = editedContent.join()
      print(joinedString)
    }

    static List<String> readFileInList(String filePath) {
        File file = new File(filePath)
        def lines = file.readLines()
        return lines
    }

}
