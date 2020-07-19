
package site.kason.kalang.sdk.compiler;

/**
 *
 * @author Kason Yang
 */
public class MultiClassLoader extends ClassLoader{

    ClassLoader[] classLoaders;
    
    public MultiClassLoader(ClassLoader... classLoaders) {
        this.classLoaders = classLoaders;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        for(ClassLoader cl:classLoaders){
            try{
                Class<?> clazz = cl.loadClass(name);
                return clazz;
            }catch(ClassNotFoundException ex){
                
            }
        }
        throw new ClassNotFoundException(name);
    }

}
