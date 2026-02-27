public class get_bakedquad {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("net.minecraft.client.renderer.block.model.BakedQuad");
        for (java.lang.reflect.Constructor<?> constructor : clazz.getConstructors()) {
            System.out.println(constructor);
        }
    }
}
